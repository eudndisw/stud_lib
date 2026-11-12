package com.library.studentlibrary.repository;

import com.library.studentlibrary.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    /** Пошук із пагінацією; автор і категорія підвантажуються одним запитом (без N+1). */
    @Override
    @EntityGraph(attributePaths = {"author", "category"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    /** Блокування рядка книги на час видачі/повернення (захист від гонки за останній примірник). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.bookId = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Integer id);

    /** Книги, які є в наявності (для форми видачі). */
    List<Book> findByAvailableQuantityGreaterThanOrderByTitleAsc(int quantity);

    boolean existsByAuthorAuthorId(Integer authorId);

    boolean existsByCategoryCategoryId(Integer categoryId);
}
