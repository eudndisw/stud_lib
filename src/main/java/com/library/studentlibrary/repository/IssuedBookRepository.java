package com.library.studentlibrary.repository;

import com.library.studentlibrary.entity.IssuedBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssuedBookRepository extends JpaRepository<IssuedBook, Integer> {

    /** Кількість активних (не повернених) видач читача. */
    long countByUserUserIdAndReturnDateIsNull(Integer userId);

    /** Чи є в читача вже активна видача цієї книги. */
    boolean existsByUserUserIdAndBookBookIdAndReturnDateIsNull(Integer userId, Integer bookId);

    /** Усі активні видачі. */
    List<IssuedBook> findByReturnDateIsNull();
}
