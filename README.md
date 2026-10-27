# Студентська бібліотека

Веб-застосунок для автоматизації студентської бібліотеки: облік книжкового фонду, читачів, авторів і категорій, видача та повернення літератури з автоматичним контролем ліміту.

## Стек технологій
* **Backend:** Java 17, Spring Boot 3.2, Spring MVC, Spring Data JPA (Hibernate), Spring Security 6
* **Frontend:** Thymeleaf, Bootstrap 5, HTML5/CSS3
* **База даних:** PostgreSQL 15
* **Збірка:** Apache Maven 3.9
* **Тести:** JUnit 5, Mockito
* **Контейнеризація:** Docker (PostgreSQL)

## Команда та ролі
* **Якубовська О.В. (Backend & Database Engineer)**
    * Розробка бізнес-логіки (Spring Boot)
    * Проєктування та адміністрування бази даних (PostgreSQL)
    * Налаштування авторизації та безпеки (Spring Security)

* **Устілова С.В. (Frontend Engineer & QA)**
    * Розробка користувацького інтерфейсу (Thymeleaf, Bootstrap 5, CSS3)
    * Валідація даних та тестування функціоналу
    * Підготовка документації проєкту

Гілки:
- `feature/backend-core` — Якубовська О. В.
- `feature/frontend-layout` — Устілова С. В.

## Архітектура
Застосунок за шаблоном MVC зі структурою **Controller → Service → Repository**.

```
stud_lib/
├── docs/                         # документація та схема БД
│   └── db_schema.sql             # схема БД і початкові дані
├── src/
│   ├── main/
│   │   ├── java/com/library/studentlibrary/
│   │   │   ├── config/           # SecurityConfig
│   │   │   ├── controller/       # Home, User, Book, Author, Category, IssuedBook
│   │   │   ├── entity/           # User, Role, Book, Author, Category, IssuedBook
│   │   │   ├── exception/        # BusinessException
│   │   │   ├── repository/       # інтерфейси Spring Data JPA
│   │   │   └── service/          # інтерфейси сервісів
│   │   │       └── impl/         # реалізації сервісів, CustomUserDetailsService
│   │   └── resources/
│   │       ├── static/css/       # style.css
│   │       ├── templates/        # Thymeleaf-шаблони
│   │       │   └── fragments/    # layout.html
│   │       └── application.properties
│   └── test/                     # unit-тести
├── .gitignore
├── docker-compose.yml            # запуск PostgreSQL
├── pom.xml
└── README.md
```

## Запуск програми
1. Встановити JDK 17, Maven 3.9 і Docker.
2. Підняти базу даних (схема та початкові дані з `docs/db_schema.sql` застосовуються автоматично):
   ```bash
   docker compose up -d
   ```
3. За потреби змінити доступи до БД у `src/main/resources/application.properties`.
4. Запустити застосунок з IntelliJ IDEA (клас `StudentLibraryApplication`).
5. Відкрити <http://localhost:8082>.

**Демо-акаунти**

| Email | Пароль | Роль |
|---|---|---|
| admin@library.com | admin123 | Адміністратор |
| librarian@library.com | librarian123 | Бібліотекар |
| student@library.com | student123 | Студент |