package com.comicverse.controller;

import com.comicverse.model.User;
import com.comicverse.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("adminVerified");

        if (verified == null || !verified) {
            return "redirect:/admin/verify-otp";
        }

        return "admin/dashboard";
    }

    // -------------------------------
    //      TRANG PROFILE ADMIN
    // -------------------------------
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("adminVerified");

        if (verified == null || !verified) {
            return "redirect:/admin/verify-otp";
        }

        User admin = (User) session.getAttribute("admin");

        if (admin == null) {
            return "redirect:/login";
        }

        model.addAttribute("admin", admin);

        return "admin/profile";
    }
}
