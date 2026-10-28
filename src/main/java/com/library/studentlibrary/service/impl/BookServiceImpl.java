package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book saveBook(Book book) {
        if (book.getAvailableQuantity() == null) {
            book.setAvailableQuantity(book.getTotalQuantity());
        }
        return bookRepository.save(book);
    }
}
