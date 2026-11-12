package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.dto.BookForm;
import com.library.studentlibrary.entity.Author;
import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.entity.Category;
import com.library.studentlibrary.entity.IssuedBook;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.AuthorRepository;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.repository.CategoryRepository;
import com.library.studentlibrary.repository.IssuedBookRepository;
import com.library.studentlibrary.service.BookService;
import com.library.studentlibrary.service.PageUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final IssuedBookRepository issuedBookRepository;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           CategoryRepository categoryRepository,
                           IssuedBookRepository issuedBookRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.issuedBookRepository = issuedBookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> search(String query, String sortField, String direction, int page, int size) {
        return bookRepository.findAll(matches(query),
                PageUtil.of(page, size, buildSort(sortField, direction)));
    }

    @Override
    @Transactional(readOnly = true)
    public Book getById(Integer id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Книгу не знайдено"));
    }

    @Override
    @Transactional
    public Book create(BookForm form) {
        Book book = new Book();
        book.setTitle(form.getTitle().trim());
        book.setYear(form.getYear().shortValue());
        book.setTotalQuantity(form.getTotalQuantity());
        book.setAvailableQuantity(form.getTotalQuantity());
        book.setAuthor(resolveAuthor(form.getAuthorId()));
        book.setCategory(resolveCategory(form.getCategoryId()));
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book update(Integer id, BookForm form) {
        Book book = getById(id);
        int issued = book.getTotalQuantity() - book.getAvailableQuantity();
        if (form.getTotalQuantity() < issued) {
            throw new BusinessException("Загальна кількість не може бути меншою за кількість виданих примірників ("
                    + issued + ")");
        }
        book.setTitle(form.getTitle().trim());
        book.setYear(form.getYear().shortValue());
        book.setTotalQuantity(form.getTotalQuantity());
        book.setAvailableQuantity(form.getTotalQuantity() - issued);
        book.setAuthor(resolveAuthor(form.getAuthorId()));
        book.setCategory(resolveCategory(form.getCategoryId()));
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Book book = getById(id);
        if (issuedBookRepository.existsByBookBookId(id)) {
            throw new BusinessException("Не можна видалити книгу, для якої є записи про видачу");
        }
        bookRepository.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableQuantityGreaterThanOrderByTitleAsc(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<String>> getHolders(Collection<Book> books) {
        Map<Integer, List<String>> result = new HashMap<>();
        if (books == null || books.isEmpty()) {
            return result;
        }
        List<Integer> ids = books.stream().map(Book::getBookId).toList();
        for (IssuedBook issue : issuedBookRepository.findByBookBookIdInAndReturnDateIsNull(ids)) {
            result.computeIfAbsent(issue.getBook().getBookId(), key -> new ArrayList<>())
                    .add(issue.getUser().getFullName());
        }
        return result;
    }

    // ---------- допоміжні методи ----------

    private Author resolveAuthor(Integer id) {
        if (id == null) {
            return null;
        }
        return authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Автора не знайдено"));
    }

    private Category resolveCategory(Integer id) {
        if (id == null) {
            return null;
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Категорію не знайдено"));
    }

    /** Кожне слово запиту має збігатися з назвою, автором або категорією (AND між словами). */
    private static Specification<Book> matches(String query) {
        List<String> patterns = PageUtil.likePatterns(query);
        if (patterns.isEmpty()) {
            return (root, cq, cb) -> cb.conjunction();
        }
        return (root, cq, cb) -> {
            Join<Book, Author> author = root.join("author", JoinType.LEFT);
            Join<Book, Category> category = root.join("category", JoinType.LEFT);
            List<Predicate> perWord = new ArrayList<>();
            for (String pattern : patterns) {
                perWord.add(cb.or(
                        cb.like(cb.lower(root.<String>get("title")), pattern, '\\'),
                        cb.like(cb.lower(author.<String>get("firstName")), pattern, '\\'),
                        cb.like(cb.lower(author.<String>get("lastName")), pattern, '\\'),
                        cb.like(cb.lower(category.<String>get("categoryName")), pattern, '\\')));
            }
            return cb.and(perWord.toArray(new Predicate[0]));
        };
    }

    private static Sort buildSort(String field, String direction) {
        Sort.Direction dir = PageUtil.direction(direction);
        List<Sort.Order> orders = new ArrayList<>();
        String key = field == null ? "" : field;
        switch (key) {
            case "author" -> {
                orders.add(new Sort.Order(dir, "author.lastName").ignoreCase());
                orders.add(new Sort.Order(dir, "author.firstName").ignoreCase());
                orders.add(Sort.Order.asc("title").ignoreCase());
            }
            case "year" -> {
                orders.add(new Sort.Order(dir, "year"));
                orders.add(Sort.Order.asc("title").ignoreCase());
            }
            default -> orders.add(new Sort.Order(dir, "title").ignoreCase());
        }
        orders.add(Sort.Order.asc("bookId"));
        return Sort.by(orders);
    }
}
