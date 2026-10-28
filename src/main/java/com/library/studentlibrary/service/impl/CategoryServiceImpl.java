package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Category;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.CategoryRepository;
import com.library.studentlibrary.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category saveCategory(Category category) {
        if (category.getCategoryId() == null
                && categoryRepository.findByCategoryName(category.getCategoryName()).isPresent()) {
            throw new BusinessException("Категорія з такою назвою вже існує!");
        }
        return categoryRepository.save(category);
    }
}
