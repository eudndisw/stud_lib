package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.dto.UserForm;
import com.library.studentlibrary.entity.Role;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.repository.RoleRepository;
import com.library.studentlibrary.repository.UserRepository;
import com.library.studentlibrary.service.PageUtil;
import com.library.studentlibrary.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IssuedBookRepository issuedBookRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           IssuedBookRepository issuedBookRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.issuedBookRepository = issuedBookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(String query, String sortField, String direction, int page, int size) {
        return userRepository.findAll(matches(query),
                PageUtil.of(page, size, buildSort(sortField, direction)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("lastName", "firstName"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Користувача не знайдено"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new BusinessException("Користувача не знайдено"));
    }

    @Override
    @Transactional
    public User create(UserForm form) {
        String email = normalizeEmail(form.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Користувач з таким email вже існує!");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("Пароль є обов’язковим для нового користувача");
        }
        validatePassword(form.getPassword());

        User user = new User();
        apply(user, form, email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Integer id, UserForm form) {
        User user = getById(id);
        String email = normalizeEmail(form.getEmail());
        userRepository.findByEmail(email)
                .filter(other -> !other.getUserId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Користувач з таким email вже існує!");
                });

        apply(user, form, email);
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            validatePassword(form.getPassword());
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Integer id, String currentEmail) {
        User user = getById(id);
        if (currentEmail != null && user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new BusinessException("Не можна видалити власний обліковий запис");
        }
        if (issuedBookRepository.existsByUserUserId(id)) {
            throw new BusinessException("Не можна видалити читача, для якого є записи про видачі");
        }
        userRepository.delete(user);
    }

    // ---------- допоміжні методи ----------

    private void apply(User user, UserForm form, String email) {
        Role role = roleRepository.findById(form.getRoleId())
                .orElseThrow(() -> new BusinessException("Роль не знайдено"));
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        String group = form.getGroupName() == null ? "" : form.getGroupName().trim();
        user.setGroupName(group.isEmpty() ? null : group);
        user.setEmail(email);
        user.setRole(role);
    }

    private static void validatePassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Пароль має містити щонайменше " + MIN_PASSWORD_LENGTH + " символів");
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /** Кожне слово запиту має збігатися з іменем, прізвищем, групою або email. */
    private static Specification<User> matches(String query) {
        List<String> patterns = PageUtil.likePatterns(query);
        if (patterns.isEmpty()) {
            return (root, cq, cb) -> cb.conjunction();
        }
        return (root, cq, cb) -> {
            List<Predicate> perWord = new ArrayList<>();
            for (String pattern : patterns) {
                perWord.add(cb.or(
                        cb.like(cb.lower(root.<String>get("firstName")), pattern, '\\'),
                        cb.like(cb.lower(root.<String>get("lastName")), pattern, '\\'),
                        cb.like(cb.lower(root.<String>get("groupName")), pattern, '\\'),
                        cb.like(cb.lower(root.<String>get("email")), pattern, '\\')));
            }
            return cb.and(perWord.toArray(new Predicate[0]));
        };
    }

    /** Сортування за іменем, прізвищем або академічною групою. */
    private static Sort buildSort(String field, String direction) {
        Sort.Direction dir = PageUtil.direction(direction);
        List<Sort.Order> orders = new ArrayList<>();
        String key = field == null ? "" : field;
        switch (key) {
            case "firstName" -> {
                orders.add(new Sort.Order(dir, "firstName").ignoreCase());
                orders.add(Sort.Order.asc("lastName").ignoreCase());
            }
            case "groupName" -> {
                orders.add(new Sort.Order(dir, "groupName").ignoreCase());
                orders.add(Sort.Order.asc("lastName").ignoreCase());
            }
            default -> {
                orders.add(new Sort.Order(dir, "lastName").ignoreCase());
                orders.add(Sort.Order.asc("firstName").ignoreCase());
            }
        }
        orders.add(Sort.Order.asc("userId"));
        return Sort.by(orders);
    }
}
