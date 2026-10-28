package com.library.studentlibrary.controller;

import com.library.studentlibrary.exception.BusinessException;
import com.library.studentlibrary.service.BookService;
import com.library.studentlibrary.service.IssuedBookService;
import com.library.studentlibrary.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/issues")
public class IssuedBookController {

    private final IssuedBookService issuedBookService;
    private final UserService userService;
    private final BookService bookService;

    public IssuedBookController(IssuedBookService issuedBookService,
                                UserService userService,
                                BookService bookService) {
        this.issuedBookService = issuedBookService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping
    public String listIssues(Model model) {
        model.addAttribute("issues", issuedBookService.getActiveIssues());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("books", bookService.getAllBooks());
        return "issues";
    }

    @PostMapping("/issue")
    public String issueBook(@RequestParam Integer userId,
                            @RequestParam Integer bookId,
                            RedirectAttributes redirect) {
        try {
            issuedBookService.issueBook(userId, bookId);
            redirect.addFlashAttribute("success", "Книга видана успішно");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/issues";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            issuedBookService.returnBook(id);
            redirect.addFlashAttribute("success", "Книгу повернено");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/issues";
    }
}
