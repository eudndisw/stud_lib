package com.library.studentlibrary.service;

import com.library.studentlibrary.dto.UserForm;
import com.library.studentlibrary.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    /** Пошук за ключовими словами (ім’я, прізвище, група, email), сортування, пагінація. */
    Page<User> search(String query, String sortField, String direction, int page, int size);

    List<User> getAllUsers();

    User getById(Integer id);

    User getByEmail(String email);

    /** Створює читача: перевіряє унікальність email, хешує пароль (BCrypt). */
    User create(UserForm form);

    /** Оновлює читача; пароль змінюється лише якщо вказаний. */
    User update(Integer id, UserForm form);

    /** Видаляє читача (не можна видалити себе або читача з історією видач). */
    void delete(Integer id, String currentEmail);
}
