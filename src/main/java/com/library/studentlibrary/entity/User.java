package com.library.studentlibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Користувач бібліотеки. Зберігає лише хеш пароля (BCrypt). */
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @NotBlank(message = "Ім’я не може бути порожнім")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Прізвище не може бути порожнім")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "group_name")
    private String groupName;

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Невірний формат email")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
