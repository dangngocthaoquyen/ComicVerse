package com.comicverse.controller;

import com.comicverse.model.User;
import com.comicverse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {

        // ✅ Lấy message từ session (do failureHandler set)
        Object msg = session.getAttribute("loginError");
        if (msg != null) {
            model.addAttribute("loginError", msg.toString());
            session.removeAttribute("loginError"); // ✅ remove ở controller (an toàn)
        }

        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user) {
        userService.register(user);
        return "redirect:/login";
    }

    // ❌ KHÔNG làm /logout ở controller nữa (Spring Security xử lý)
}
