-- =====================================================================
-- Схема БД «Студентська бібліотека» (PostgreSQL 15)
-- Відповідає JPA-сутностям у src/main/java/.../entity.
-- Скрипт ідемпотентний: його можна виконувати повторно.
-- Застосовується автоматично при першому створенні контейнера (docker-compose.yml).
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- хешування паролів (BCrypt) у початкових даних
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- швидкий пошук підрядка (LIKE '%...%')

-- ---------- Таблиці ----------

CREATE TABLE IF NOT EXISTS roles (
    role_id   SERIAL       PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE            -- ADMIN, LIBRARIAN, STUDENT
);

CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL       PRIMARY KEY,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    group_name    VARCHAR(255),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,              -- лише BCrypt-хеш (НФВ безпеки)
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
    category_id        INTEGER      REFERENCES categories (category_id),
    -- цілісність: доступних примірників не може бути менше 0 або більше загальної кількості
    CONSTRAINT chk_books_available CHECK (available_quantity >= 0 AND available_quantity <= total_quantity)
);

CREATE TABLE IF NOT EXISTS issued_books (
    issue_id    SERIAL  PRIMARY KEY,
    issue_date  DATE    NOT NULL,
    return_date DATE,                                  -- NULL = книга ще у читача
    user_id     INTEGER NOT NULL REFERENCES users (user_id),
    book_id     INTEGER NOT NULL REFERENCES books (book_id)
);

-- ---------- Індекси (НФВ: відгук каталогу до 800 мс при 50 користувачах) ----------
-- Назви збігаються з @Index у сутностях, тож Hibernate не створює дублікатів.

CREATE INDEX IF NOT EXISTS idx_books_title       ON books (title);
CREATE INDEX IF NOT EXISTS idx_books_author      ON books (author_id);
CREATE INDEX IF NOT EXISTS idx_books_category    ON books (category_id);
CREATE INDEX IF NOT EXISTS idx_users_last_name   ON users (last_name);
CREATE INDEX IF NOT EXISTS idx_users_group       ON users (group_name);
CREATE INDEX IF NOT EXISTS idx_authors_last_name ON authors (last_name);
CREATE INDEX IF NOT EXISTS idx_issued_user       ON issued_books (user_id);
CREATE INDEX IF NOT EXISTS idx_issued_book       ON issued_books (book_id);

-- Пошук за підрядком у назві книги (lower(title) LIKE '%слово%')
CREATE INDEX IF NOT EXISTS idx_books_title_trgm ON books USING gin (lower(title) gin_trgm_ops);

-- Цілісність: одну й ту саму книгу не можна видати одному читачу двічі одночасно
CREATE UNIQUE INDEX IF NOT EXISTS uq_issued_active
    ON issued_books (user_id, book_id) WHERE return_date IS NULL;

-- ---------- Початкові дані (лише для розробки!) ----------
-- Паролі хешуються прямо в БД: crypt(пароль, gen_salt('bf', 10)) дає BCrypt-хеш.

INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('LIBRARIAN'),
    ('STUDENT')
ON CONFLICT (role_name) DO NOTHING;

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
