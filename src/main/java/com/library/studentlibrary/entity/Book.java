package com.library.studentlibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Книга (одиниця бібліотечного фонду). */
@Entity
@Table(name = "books")
@Getter @Setter @NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @NotBlank(message = "Назва книги є обов’язковою")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Рік є обов’язковим")
    @Min(value = 1500, message = "Рік має бути не менше 1500")
    @Max(value = 2100, message = "Рік має бути коректним")
    @Column(name = "year")
    private Short year;

    @NotNull(message = "Загальна кількість є обов’язковою")
    @Min(value = 1, message = "Кількість має бути не менше 1")
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public boolean isAvailable() {
        return availableQuantity != null && availableQuantity > 0;
    }
}
