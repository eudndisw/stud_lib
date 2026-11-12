package com.library.studentlibrary.service;

import com.library.studentlibrary.dto.BookForm;
import com.library.studentlibrary.entity.Author;
import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.entity.IssuedBook;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.AuthorRepository;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.CategoryRepository;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private IssuedBookRepository issuedBookRepository;

    @InjectMocks private BookServiceImpl service;

    private static BookForm form(int total) {
        BookForm form = new BookForm();
        form.setTitle("  Кобзар ");
        form.setYear(1840);
        form.setTotalQuantity(total);
        return form;
    }

    private static Book book(int id, int total, int available) {
        Book book = new Book();
        book.setBookId(id);
        book.setTitle("Книга");
        book.setTotalQuantity(total);
        book.setAvailableQuantity(available);
        return book;
    }

    private Pageable searchAndCapturePageable(String sort, String dir, int page, int size) {
        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        service.search("кобзар", sort, dir, page, size);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(ArgumentMatchers.<Specification<Book>>any(), captor.capture());
        return captor.getValue();
    }

    @Test
    void create_setsAvailableEqualToTotal_andTrimsTitle() {
        Author author = new Author();
        author.setAuthorId(1);
        BookForm form = form(3);
        form.setAuthorId(1);
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).then(returnsFirstArg());

        Book saved = service.create(form);

        assertEquals("Кобзар", saved.getTitle());
        assertEquals(3, saved.getAvailableQuantity());
        assertEquals(author, saved.getAuthor());
        assertNull(saved.getCategory());
    }

    @Test
    void update_recalculatesAvailableQuantity() {
        Book book = book(1, 5, 2); // 3 примірники видано
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).then(returnsFirstArg());

        Book saved = service.update(1, form(10));

        assertEquals(10, saved.getTotalQuantity());
        assertEquals(7, saved.getAvailableQuantity());
    }

    @Test
    void update_totalLessThanIssued_throws() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book(1, 5, 2)));

        assertThrows(BusinessException.class, () -> service.update(1, form(2)));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_bookWithIssueHistory_throws() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book(1, 3, 3)));
        when(issuedBookRepository.existsByBookBookId(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(1));
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void delete_bookWithoutHistory_deletes() {
        Book book = book(1, 3, 3);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(issuedBookRepository.existsByBookBookId(1)).thenReturn(false);

        service.delete(1);

        verify(bookRepository).delete(book);
    }

    @Test
    void search_limitsPageSizeTo50() {
        Pageable pageable = searchAndCapturePageable("title", "asc", 0, 1000);

        assertEquals(50, pageable.getPageSize());
    }

    @Test
    void search_sortByAuthor_usesAuthorLastName() {
        Pageable pageable = searchAndCapturePageable("author", "desc", 2, 10);

        assertEquals(2, pageable.getPageNumber());
        assertEquals("author.lastName", pageable.getSort().iterator().next().getProperty());
        assertTrue(pageable.getSort().iterator().next().isDescending());
    }

    @Test
    void search_unknownSortField_fallsBackToTitle() {
        Pageable pageable = searchAndCapturePageable("hack; drop table", "asc", 0, 10);

        assertEquals("title", pageable.getSort().iterator().next().getProperty());
    }

    @Test
    void getHolders_groupsReaderNamesByBook() {
        Book first = book(1, 2, 0);
        Book second = book(2, 1, 1);
        User ivan = new User();
        ivan.setFirstName("Іван");
        ivan.setLastName("Петренко");
        User maria = new User();
        maria.setFirstName("Марія");
        maria.setLastName("Коваленко");
        IssuedBook a = new IssuedBook();
        a.setBook(first);
        a.setUser(ivan);
        IssuedBook b = new IssuedBook();
        b.setBook(first);
        b.setUser(maria);
        when(issuedBookRepository.findByBookBookIdInAndReturnDateIsNull(List.of(1, 2))).thenReturn(List.of(a, b));

        Map<Integer, List<String>> holders = service.getHolders(List.of(first, second));

        assertEquals(List.of("Іван Петренко", "Марія Коваленко"), holders.get(1));
        assertNull(holders.get(2));
    }
}