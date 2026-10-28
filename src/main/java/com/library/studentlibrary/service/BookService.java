package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Book;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();

    Book saveBook(Book book);
}
