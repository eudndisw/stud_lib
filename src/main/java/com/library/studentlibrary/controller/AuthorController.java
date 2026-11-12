package com.library.studentlibrary.controller;

import com.library.studentlibrary.entity.Author;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /** Перегляд: ADMIN і LIBRARIAN. Зміни: лише ADMIN (див. SecurityConfig). */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        return "authors";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        prepareForm(model, new Author(), "Новий автор", "/authors");
        return "author-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") Author form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Новий автор", "/authors");
            return "author-form";
        }
        try {
            authorService.create(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Новий автор", "/authors");
            return "author-form";
        }
        redirect.addFlashAttribute("success", "Автора додано");
        return "redirect:/authors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepareForm(model, authorService.getById(id), "Редагування автора", "/authors/" + id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/authors";
        }
        return "author-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") Author form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Редагування автора", "/authors/" + id);
            return "author-form";
        }
        try {
            authorService.update(id, form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Редагування автора", "/authors/" + id);
            return "author-form";
        }
        redirect.addFlashAttribute("success", "Зміни збережено");
        return "redirect:/authors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            authorService.delete(id);
            redirect.addFlashAttribute("success", "Автора видалено");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/authors";
    }

    private void prepareForm(Model model, Author form, String pageTitle, String formAction) {
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }
}
