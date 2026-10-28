package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.UserRepository;
import com.library.studentlibrary.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Користувач з таким email вже існує!");
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}
