package com.library.studentlibrary.service;

import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.entity.IssuedBook;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.repository.UserRepository;
import com.library.studentlibrary.service.impl.IssuedBookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit-тести бізнес-логіки видачі/повернення. */
@ExtendWith(MockitoExtension.class)
class IssuedBookServiceImplTest {

    @Mock private IssuedBookRepository issuedBookRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private IssuedBookServiceImpl service;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        book = new Book();
        book.setBookId(10);
        book.setTotalQuantity(3);
        book.setAvailableQuantity(3);

        lenient().when(userRepository.findById(1)).thenReturn(Optional.of(user));
        lenient().when(bookRepository.findById(10)).thenReturn(Optional.of(book));
    }

    @Test
    void issueBook_success_decreasesAvailableQuantity() {
        when(issuedBookRepository.countByUserUserIdAndReturnDateIsNull(1)).thenReturn(0L);
        when(issuedBookRepository.existsByUserUserIdAndBookBookIdAndReturnDateIsNull(1, 10)).thenReturn(false);

        service.issueBook(1, 10);

        assertEquals(2, book.getAvailableQuantity());
        verify(issuedBookRepository).save(any(IssuedBook.class));
        verify(bookRepository).save(book);
    }

    @Test
    void issueBook_limitReached_throws() {
        when(issuedBookRepository.countByUserUserIdAndReturnDateIsNull(1)).thenReturn(5L);

        assertThrows(BusinessException.class, () -> service.issueBook(1, 10));
        verify(issuedBookRepository, never()).save(any());
    }

    @Test
    void issueBook_noCopiesAvailable_throws() {
        book.setAvailableQuantity(0);
        when(issuedBookRepository.countByUserUserIdAndReturnDateIsNull(1)).thenReturn(0L);

        assertThrows(BusinessException.class, () -> service.issueBook(1, 10));
        verify(issuedBookRepository, never()).save(any());
    }

    @Test
    void issueBook_duplicateActiveIssue_throws() {
        when(issuedBookRepository.countByUserUserIdAndReturnDateIsNull(1)).thenReturn(1L);
        when(issuedBookRepository.existsByUserUserIdAndBookBookIdAndReturnDateIsNull(1, 10)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.issueBook(1, 10));
        verify(issuedBookRepository, never()).save(any());
    }

    @Test
    void returnBook_setsReturnDate_andIncreasesAvailableQuantity() {
        book.setAvailableQuantity(2);
        IssuedBook issue = new IssuedBook();
        issue.setIssueId(100);
        issue.setUser(user);
        issue.setBook(book);
        issue.setIssueDate(LocalDate.now().minusDays(3));
        when(issuedBookRepository.findById(100)).thenReturn(Optional.of(issue));

        service.returnBook(100);

        assertNotNull(issue.getReturnDate());
        assertEquals(3, book.getAvailableQuantity());
    }
}
