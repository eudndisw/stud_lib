package com.library.studentlibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authors")
@Getter @Setter @NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    @NotBlank(message = "Ім’я автора не може бути порожнім")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Прізвище автора не може бути порожнім")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
