package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Category;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.CategoryRepository;
import com.library.studentlibrary.service.CategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by("categoryName"));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Категорію не знайдено"));
    }

    @Override
    @Transactional
    public Category create(Category form) {
        String name = form.getCategoryName().trim();
        checkUnique(name, null);
        Category category = new Category();
        category.setCategoryName(name);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Integer id, Category form) {
        Category category = getById(id);
        String name = form.getCategoryName().trim();
        checkUnique(name, id);
        category.setCategoryName(name);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Category category = getById(id);
        if (bookRepository.existsByCategoryCategoryId(id)) {
            throw new BusinessException("Не можна видалити категорію, до якої належать книги");
        }
        categoryRepository.delete(category);
    }

    private void checkUnique(String name, Integer selfId) {
        categoryRepository.findByCategoryNameIgnoreCase(name)
                .filter(other -> !other.getCategoryId().equals(selfId))
                .ifPresent(other -> {
                    throw new BusinessException("Категорія з такою назвою вже існує!");
                });
    }
}
