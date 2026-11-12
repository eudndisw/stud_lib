package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Author;

import java.util.List;

public interface AuthorService {

    List<Author> getAllAuthors();

    Author getById(Integer id);

    Author create(Author form);

    Author update(Integer id, Author form);

    /** Не можна видалити автора, у якого є книги. */
    void delete(Integer id);
}
