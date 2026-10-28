package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    /** Створює користувача: перевіряє унікальність email і хешує пароль (BCrypt). */
    User registerUser(User user, String rawPassword);
}
