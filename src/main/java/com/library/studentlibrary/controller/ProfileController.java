package com.library.studentlibrary.controller;

import com.library.studentlibrary.entity.IssuedBook;
import com.library.studentlibrary.entity.User;
import com.library.studentlibrary.service.IssuedBookService;
import com.library.studentlibrary.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/** «Мої книги»: що зараз у читача та історія його видач. Доступно будь-якій ролі. */
@Controller
public class ProfileController {

    private final UserService userService;
    private final IssuedBookService issuedBookService;

    public ProfileController(UserService userService, IssuedBookService issuedBookService) {
        this.userService = userService;
        this.issuedBookService = issuedBookService;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        List<IssuedBook> issues = issuedBookService.getUserIssues(user.getUserId());
        long active = issues.stream().filter(i -> !i.isReturned()).count();

        model.addAttribute("user", user);
        model.addAttribute("issues", issues);
        model.addAttribute("active", active);
        model.addAttribute("limit", IssuedBookService.MAX_ACTIVE_ISSUES);
        return "profile";
    }
}
