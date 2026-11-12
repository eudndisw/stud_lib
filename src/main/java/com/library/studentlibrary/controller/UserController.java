package com.library.studentlibrary.controller;

import com.library.studentlibrary.dto.UserForm;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.service.PageUtil;
import com.library.studentlibrary.service.RoleService;
import com.library.studentlibrary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.security.Principal;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    /** Список читачів: пошук, сортування (ім’я, прізвище, група), пагінація. ADMIN і LIBRARIAN. */
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "lastName") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "" + PageUtil.DEFAULT_PAGE_SIZE) int size,
                       Model model) {
        Page<User> users = userService.search(q, sort, dir, page, size);
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "users";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        prepareForm(model, new UserForm(), "Новий читач", "/users", true);
        return "user-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") UserForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Новий читач", "/users", true);
            return "user-form";
        }
        try {
            userService.create(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Новий читач", "/users", true);
            return "user-form";
        }
        redirect.addFlashAttribute("success", "Читача додано");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepareForm(model, UserForm.from(userService.getById(id)),
                    "Редагування читача", "/users/" + id, false);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/users";
        }
        return "user-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") UserForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepareForm(model, form, "Редагування читача", "/users/" + id, false);
            return "user-form";
        }
        try {
            userService.update(id, form);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareForm(model, form, "Редагування читача", "/users/" + id, false);
            return "user-form";
        }
        redirect.addFlashAttribute("success", "Зміни збережено");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, Principal principal, RedirectAttributes redirect) {
        try {
            userService.delete(id, principal.getName());
            redirect.addFlashAttribute("success", "Читача видалено");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    private void prepareForm(Model model, UserForm form, String pageTitle, String formAction, boolean creating) {
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
        model.addAttribute("creating", creating);
        model.addAttribute("roles", roleService.getAllRoles());
    }
}
