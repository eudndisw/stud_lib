package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Author;
import com.library.studentlibrary.entity.Category;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.AuthorRepository;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.CategoryRepository;
import com.library.studentlibrary.service.impl.AuthorServiceImpl;
import com.library.studentlibrary.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorAndCategoryServiceTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BookRepository bookRepository;

    @InjectMocks private AuthorServiceImpl authorService;
    @InjectMocks private CategoryServiceImpl categoryService;

    private static Author author(Integer id, String first, String last) {
        Author author = new Author();
        author.setAuthorId(id);
        author.setFirstName(first);
        author.setLastName(last);
        return author;
    }

    private static Category category(Integer id, String name) {
        Category category = new Category();
        category.setCategoryId(id);
        category.setCategoryName(name);
        return category;
    }

    @Test
    void createAuthor_duplicate_throws() {
        when(authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Тарас", "Шевченко"))
                .thenReturn(Optional.of(author(1, "Тарас", "Шевченко")));

        assertThrows(BusinessException.class, () -> authorService.create(author(null, " Тарас ", "Шевченко")));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void deleteAuthor_withBooks_throws() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author(1, "Іван", "Франко")));
        when(bookRepository.existsByAuthorAuthorId(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> authorService.delete(1));
        verify(authorRepository, never()).delete(any());
    }

    @Test
    void createCategory_duplicate_throws() {
        when(categoryRepository.findByCategoryNameIgnoreCase("Програмування"))
                .thenReturn(Optional.of(category(1, "Програмування")));

        assertThrows(BusinessException.class, () -> categoryService.create(category(null, "Програмування")));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_withBooks_throws() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category(1, "Поезія")));
        when(bookRepository.existsByCategoryCategoryId(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> categoryService.delete(1));
        verify(categoryRepository, never()).delete(any());
    }
}
