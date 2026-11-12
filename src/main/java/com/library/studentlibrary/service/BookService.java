package com.library.studentlibrary.service;

import com.library.studentlibrary.dto.BookForm;
import com.library.studentlibrary.entity.Book;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BookService {

    /** Пошук за ключовими словами (назва, автор, категорія), сортування за title / author / year, пагінація. */
    Page<Book> search(String query, String sortField, String direction, int page, int size);

    Book getById(Integer id);

    Book create(BookForm form);

    Book update(Integer id, BookForm form);

    void delete(Integer id);

    /** Книги, які є в наявності. */
    List<Book> getAvailableBooks();

    /** Для кожної книги — імена читачів, у яких вона зараз перебуває. */
    Map<Integer, List<String>> getHolders(Collection<Book> books);
}
