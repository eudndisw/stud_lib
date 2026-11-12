package com.library.studentlibrary.service;

import com.library.studentlibrary.dto.UserForm;
import com.library.studentlibrary.entity.Role;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.repository.RoleRepository;
import com.library.studentlibrary.repository.UserRepository;
import com.library.studentlibrary.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private IssuedBookRepository issuedBookRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl service;

    private static UserForm form(String email, String password) {
        UserForm form = new UserForm();
        form.setFirstName(" Іван ");
        form.setLastName("Петренко");
        form.setGroupName("  ");
        form.setEmail(email);
        form.setPassword(password);
        form.setRoleId(3);
        return form;
    }

    private static User existingUser(int id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setPasswordHash("OLD_HASH");
        return user;
    }

    @Test
    void create_hashesPassword_normalizesEmail_andTrimsFields() {
        Role role = new Role("STUDENT");
        when(userRepository.existsByEmail("ivan@library.com")).thenReturn(false);
        when(roleRepository.findById(3)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret1")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User saved = service.create(form("  Ivan@Library.com ", "secret1"));

        assertEquals("HASH", saved.getPasswordHash());
        assertEquals("ivan@library.com", saved.getEmail());
        assertEquals("Іван", saved.getFirstName());
        assertNull(saved.getGroupName());
        assertEquals(role, saved.getRole());
    }

    @Test
    void create_duplicateEmail_throws() {
        when(userRepository.existsByEmail("ivan@library.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(form("ivan@library.com", "secret1")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_missingPassword_throws() {
        when(userRepository.existsByEmail("ivan@library.com")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(form("ivan@library.com", " ")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shortPassword_throws() {
        when(userRepository.existsByEmail("ivan@library.com")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(form("ivan@library.com", "123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_blankPassword_keepsOldHash() {
        User user = existingUser(5, "ivan@library.com");
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("ivan@library.com")).thenReturn(Optional.of(user));
        when(roleRepository.findById(3)).thenReturn(Optional.of(new Role("STUDENT")));
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User saved = service.update(5, form("ivan@library.com", ""));

        assertEquals("OLD_HASH", saved.getPasswordHash());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_emailTakenByAnotherUser_throws() {
        User user = existingUser(5, "ivan@library.com");
        User other = existingUser(6, "other@library.com");
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@library.com")).thenReturn(Optional.of(other));

        assertThrows(BusinessException.class, () -> service.update(5, form("other@library.com", "")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_ownAccount_throws() {
        when(userRepository.findById(5)).thenReturn(Optional.of(existingUser(5, "me@library.com")));

        assertThrows(BusinessException.class, () -> service.delete(5, "ME@library.com"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_userWithIssueHistory_throws() {
        when(userRepository.findById(5)).thenReturn(Optional.of(existingUser(5, "ivan@library.com")));
        when(issuedBookRepository.existsByUserUserId(5)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(5, "admin@library.com"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_userWithoutHistory_deletes() {
        User user = existingUser(5, "ivan@library.com");
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(issuedBookRepository.existsByUserUserId(5)).thenReturn(false);

        service.delete(5, "admin@library.com");

        verify(userRepository).delete(user);
    }
}