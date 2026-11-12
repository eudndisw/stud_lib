package com.library.studentlibrary.dto;

import com.library.studentlibrary.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Дані форми додавання/редагування читача. Пароль при редагуванні необов’язковий. */
@Getter @Setter @NoArgsConstructor
public class UserForm {

    @NotBlank(message = "Ім’я не може бути порожнім")
    @Size(max = 255, message = "Ім’я занадто довге")
    private String firstName;

    @NotBlank(message = "Прізвище не може бути порожнім")
    @Size(max = 255, message = "Прізвище занадто довге")
    private String lastName;

    @Size(max = 255, message = "Назва групи занадто довга")
    private String groupName;

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Невірний формат email")
    @Size(max = 255, message = "Email занадто довгий")
    private String email;

    /** Мінімум 6 символів; перевіряється в сервісі (при створенні обов’язковий). */
    private String password;

    @NotNull(message = "Оберіть роль")
    private Integer roleId;

    public static UserForm from(User user) {
        UserForm form = new UserForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setGroupName(user.getGroupName());
        form.setEmail(user.getEmail());
        form.setRoleId(user.getRole().getRoleId());
        return form;
    }
}
