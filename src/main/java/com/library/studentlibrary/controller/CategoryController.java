package com.library.studentlibrary.controller;

import com.library.studentlibrary.entity.Category;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.service.CategoryService;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** Перегляд: ADMIN і LIBRARIAN. Зміни: лише ADMIN (див. SecurityConfig). */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        prepareForm(model, new Category(), "Нова категорія", "/categories");
        return "category-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") Category form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Нова категорія", "/categories");
            return "category-form";
        }
        try {
            categoryService.create(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Нова категорія", "/categories");
            return "category-form";
        }
        redirect.addFlashAttribute("success", "Категорію додано");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepareForm(model, categoryService.getById(id), "Редагування категорії", "/categories/" + id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories";
        }
        return "category-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") Category form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Редагування категорії", "/categories/" + id);
            return "category-form";
        }
        try {
            categoryService.update(id, form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Редагування категорії", "/categories/" + id);
            return "category-form";
        }
        redirect.addFlashAttribute("success", "Зміни збережено");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            categoryService.delete(id);
            redirect.addFlashAttribute("success", "Категорію видалено");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    private void prepareForm(Model model, Category form, String pageTitle, String formAction) {
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }
}
