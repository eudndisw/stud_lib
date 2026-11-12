package com.library.studentlibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Розмежування доступу за ролями:
     * - STUDENT: каталог (пошук, сортування), «Мої книги»;
     * - LIBRARIAN: + видача/повернення, перегляд читачів, авторів, категорій;
     * - ADMIN: + керування читачами, книгами, авторами та категоріями.
     * Правила перевіряються зверху вниз, тож більш конкретні стоять першими.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/authors", "/categories").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers("/authors/**", "/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/books").authenticated()
                .requestMatchers("/books/**").hasRole("ADMIN")
                .requestMatchers("/issues/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    /** НФВ: паролі зберігаються лише у вигляді BCrypt-хешу. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
