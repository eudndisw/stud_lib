package com.library.studentlibrary.service.impl;

import com.library.studentlibrary.entity.Author;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.repository.AuthorRepository;
import com.library.studentlibrary.repository.BookRepository;
import com.library.studentlibrary.service.AuthorService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> getAllAuthors() {
        return authorRepository.findAll(Sort.by("lastName", "firstName"));
    }

    @Override
    @Transactional(readOnly = true)
    public Author getById(Integer id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Автора не знайдено"));
    }

    @Override
    @Transactional
    public Author create(Author form) {
        String first = form.getFirstName().trim();
        String last = form.getLastName().trim();
        checkUnique(first, last, null);
        Author author = new Author();
        author.setFirstName(first);
        author.setLastName(last);
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public Author update(Integer id, Author form) {
        Author author = getById(id);
        String first = form.getFirstName().trim();
        String last = form.getLastName().trim();
        checkUnique(first, last, id);
        author.setFirstName(first);
        author.setLastName(last);
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Author author = getById(id);
        if (bookRepository.existsByAuthorAuthorId(id)) {
            throw new BusinessException("Не можна видалити автора, у якого є книги");
        }
        authorRepository.delete(author);
    }

    private void checkUnique(String first, String last, Integer selfId) {
        authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(first, last)
                .filter(other -> !other.getAuthorId().equals(selfId))
                .ifPresent(other -> {
                    throw new BusinessException("Такий автор уже існує!");
                });
    }
}
