package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.IssuedBook;

import java.util.List;

public interface IssuedBookService {

    /** Максимальна кількість одночасно виданих книг на одного читача. */
    int MAX_ACTIVE_ISSUES = 5;

    List<IssuedBook> getActiveIssues();

    /** Видача книги: ліміт 5 книг, наявність примірника. */
    void issueBook(Integer userId, Integer bookId);

    /** Повернення книги */
    void returnBook(Integer issueId);
}
