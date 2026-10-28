package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Author;

import java.util.List;

public interface AuthorService {
    List<Author> getAllAuthors();

    Author saveAuthor(Author author);
}
