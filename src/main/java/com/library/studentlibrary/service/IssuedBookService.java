package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.IssuedBook;

import java.util.List;

public interface IssuedBookService {

    /** Максимальна кількість одночасно виданих книг на одного читача. */
    int MAX_ACTIVE_ISSUES = 5;

    List<IssuedBook> getActiveIssues();

    /** Уся історія видач читача (активні й повернені). */
    List<IssuedBook> getUserIssues(Integer userId);

    /** Видача книги: ліміт 5 книг, наявність примірника, відсутність дубля. */
    void issueBook(Integer userId, Integer bookId);

    /** Повернення книги: проставляє returnDate і збільшує availableQuantity. */
    void returnBook(Integer issueId);
}
