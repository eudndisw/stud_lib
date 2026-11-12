package com.library.studentlibrary.repository;

import com.library.studentlibrary.entity.IssuedBook;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IssuedBookRepository extends JpaRepository<IssuedBook, Integer> {

    /** Кількість активних (не повернених) видач читача. */
    long countByUserUserIdAndReturnDateIsNull(Integer userId);

    /** Чи є в читача вже активна видача цієї книги. */
    boolean existsByUserUserIdAndBookBookIdAndReturnDateIsNull(Integer userId, Integer bookId);

    /** Усі активні видачі (найновіші першими). */
    @EntityGraph(attributePaths = {"user", "book"})
    List<IssuedBook> findByReturnDateIsNullOrderByIssueDateDescIssueIdDesc();

    /** Активні видачі для набору книг: у кого зараз перебувають примірники. */
    @EntityGraph(attributePaths = {"user", "book"})
    List<IssuedBook> findByBookBookIdInAndReturnDateIsNull(Collection<Integer> bookIds);

    /** Уся історія читача (для сторінки «Мої книги»). */
    @EntityGraph(attributePaths = {"book"})
    List<IssuedBook> findByUserUserIdOrderByIssueDateDescIssueIdDesc(Integer userId);

    boolean existsByBookBookId(Integer bookId);

    boolean existsByUserUserId(Integer userId);
}
