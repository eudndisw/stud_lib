package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getById(Integer id);

    Category create(Category form);

    Category update(Integer id, Category form);

    /** Не можна видалити категорію, до якої належать книги. */
    void delete(Integer id);
}
