-- Схема БД «Студентська бібліотека»
CREATE TABLE IF NOT EXISTS roles (
    role_id   SERIAL       PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL       PRIMARY KEY,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    group_name    VARCHAR(255),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id       INTEGER      NOT NULL REFERENCES roles (role_id)
    );

CREATE TABLE IF NOT EXISTS authors (
    author_id  SERIAL       PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS categories (
    category_id   SERIAL       PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS books (
    book_id            SERIAL       PRIMARY KEY,
    title              VARCHAR(255) NOT NULL,
    year               SMALLINT     NOT NULL CHECK (year BETWEEN 1500 AND 2100),
    total_quantity     INTEGER      NOT NULL CHECK (total_quantity >= 1),
    available_quantity INTEGER      NOT NULL,
    author_id          INTEGER      REFERENCES authors (author_id),
    category_id        INTEGER      REFERENCES categories (category_id)
    );

CREATE TABLE IF NOT EXISTS issued_books (
    issue_id    SERIAL  PRIMARY KEY,
    issue_date  DATE    NOT NULL,
    return_date DATE,
    user_id     INTEGER NOT NULL REFERENCES users (user_id),
    book_id     INTEGER NOT NULL REFERENCES books (book_id)
    );

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('LIBRARIAN'),
                                  ('STUDENT')
    ON CONFLICT (role_name) DO NOTHING;

-- Демо-акаунти
INSERT INTO users (first_name, last_name, group_name, email, password_hash, role_id) VALUES
    ('Адмін', 'Системи', NULL, 'admin@library.com',
    crypt('admin123', gen_salt('bf', 10)),
    (SELECT role_id FROM roles WHERE role_name = 'ADMIN')),
    ('Марія', 'Коваленко', NULL, 'librarian@library.com',
    crypt('librarian123', gen_salt('bf', 10)),
    (SELECT role_id FROM roles WHERE role_name = 'LIBRARIAN')),
    ('Іван', 'Петренко', 'ІО-45', 'student@library.com',
    crypt('student123', gen_salt('bf', 10)),
    (SELECT role_id FROM roles WHERE role_name = 'STUDENT'))
    ON CONFLICT (email) DO NOTHING;