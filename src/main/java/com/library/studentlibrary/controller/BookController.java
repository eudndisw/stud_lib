package com.library.studentlibrary.controller;

import com.library.studentlibrary.dto.BookForm;
import com.library.studentlibrary.entity.Book;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.service.AuthorService;
import com.library.studentlibrary.service.BookService;
import com.library.studentlibrary.service.CategoryService;
import com.library.studentlibrary.service.PageUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    public BookController(BookService bookService,
                          AuthorService authorService,
                          CategoryService categoryService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.categoryService = categoryService;
    }

    /** Каталог: пошук, сортування, пагінація (до 50 записів на сторінку). Доступний усім ролям. */
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "title") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "" + PageUtil.DEFAULT_PAGE_SIZE) int size,
                       Authentication authentication,
                       Model model) {
        Page<Book> books = bookService.search(q, sort, dir, page, size);
        boolean staff = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
        Map<Integer, List<String>> holders = staff ? bookService.getHolders(books.getContent()) : Map.of();

        model.addAttribute("books", books);
        model.addAttribute("holders", holders);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "books";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        prepareForm(model, new BookForm(), "Нова книга", "/books");
        return "book-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Нова книга", "/books");
            return "book-form";
        }
        try {
            bookService.create(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Нова книга", "/books");
            return "book-form";
        }
        redirect.addFlashAttribute("success", "Книгу додано");
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepareForm(model, BookForm.from(bookService.getById(id)), "Редагування книги", "/books/" + id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/books";
        }
        return "book-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") BookForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Редагування книги", "/books/" + id);
            return "book-form";
        }
        try {
            bookService.update(id, form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Редагування книги", "/books/" + id);
            return "book-form";
        }
        redirect.addFlashAttribute("success", "Зміни збережено");
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            bookService.delete(id);
            redirect.addFlashAttribute("success", "Книгу видалено");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }

    private void prepareForm(Model model, BookForm form, String pageTitle, String formAction) {
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("categories", categoryService.getAllCategories());
    }
}
