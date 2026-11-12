package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.entity.IssuedBook;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.repository.UserRepository;
import com.library.studentlibrary.service.IssuedBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Видача й повернення книг. Рядок книги блокується (PESSIMISTIC_WRITE),
 * тож два бібліотекарі не можуть одночасно видати останній примірник.
 */
@Service
public class IssuedBookServiceImpl implements IssuedBookService {

    private final IssuedBookRepository issuedBookRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public IssuedBookServiceImpl(IssuedBookRepository issuedBookRepository,
                                 BookRepository bookRepository,
                                 UserRepository userRepository) {
        this.issuedBookRepository = issuedBookRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssuedBook> getActiveIssues() {
        return issuedBookRepository.findByReturnDateIsNullOrderByIssueDateDescIssueIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssuedBook> getUserIssues(Integer userId) {
        return issuedBookRepository.findByUserUserIdOrderByIssueDateDescIssueIdDesc(userId);
    }

    @Override
    @Transactional
    public void issueBook(Integer userId, Integer bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Користувача не знайдено"));
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BusinessException("Книгу не знайдено"));

        // 1. Ліміт активних книг
        if (issuedBookRepository.countByUserUserIdAndReturnDateIsNull(userId) >= MAX_ACTIVE_ISSUES) {
            throw new BusinessException("Перевищено ліміт " + MAX_ACTIVE_ISSUES + " книг!");
        }
        // 2. Наявність примірників
        if (!book.isAvailable()) {
            throw new BusinessException("Книги немає в наявності!");
        }
        // 3. Дубль активної видачі
        if (issuedBookRepository.existsByUserUserIdAndBookBookIdAndReturnDateIsNull(userId, bookId)) {
            throw new BusinessException("Ця книга вже видана цьому читачу!");
        }

        IssuedBook issue = new IssuedBook();
        issue.setUser(user);
        issue.setBook(book);
        issue.setIssueDate(LocalDate.now());
        issuedBookRepository.save(issue);

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void returnBook(Integer issueId) {
        IssuedBook issue = issuedBookRepository.findById(issueId)
                .orElseThrow(() -> new BusinessException("Запис про видачу не знайдено"));
        if (issue.isReturned()) {
            throw new BusinessException("Книгу вже повернено!");
        }
        Book book = bookRepository.findByIdForUpdate(issue.getBook().getBookId())
                .orElseThrow(() -> new BusinessException("Книгу не знайдено"));

        issue.setReturnDate(LocalDate.now());
        issuedBookRepository.save(issue);

        book.setAvailableQuantity(Math.min(book.getTotalQuantity(), book.getAvailableQuantity() + 1));
        bookRepository.save(book);
    }
}
