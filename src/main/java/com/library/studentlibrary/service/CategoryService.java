package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    Category saveCategory(Category category);
}
