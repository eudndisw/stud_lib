package com.library.studentlibrary.dto;

import com.library.studentlibrary.entity.Book;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Дані форми додавання/редагування книги. */
@Getter @Setter @NoArgsConstructor
public class BookForm {

    @NotBlank(message = "Назва книги є обов’язковою")
    @Size(max = 255, message = "Назва не може бути довшою за 255 символів")
    private String title;

    @NotNull(message = "Вкажіть рік видання")
    @Min(value = 1500, message = "Рік має бути не менше 1500")
    @Max(value = 2100, message = "Рік має бути не більше 2100")
    private Integer year;

    @NotNull(message = "Вкажіть загальну кількість примірників")
    @Min(value = 1, message = "Кількість має бути не менше 1")
    @Max(value = 100000, message = "Кількість має бути коректною")
    private Integer totalQuantity;

    private Integer authorId;

    private Integer categoryId;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setYear(book.getYear() == null ? null : book.getYear().intValue());
        form.setTotalQuantity(book.getTotalQuantity());
        form.setAuthorId(book.getAuthor() == null ? null : book.getAuthor().getAuthorId());
        form.setCategoryId(book.getCategory() == null ? null : book.getCategory().getCategoryId());
        return form;
    }
}
