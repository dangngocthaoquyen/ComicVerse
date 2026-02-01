package com.comicverse.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("adminVerified");
        if (verified == null || !verified) {
            return "redirect:/admin/verify-otp";
        }

        return "admin/dashboard";
    }

    // ❌ Bỏ /profile ở đây để tránh trùng mapping với AdminProfileController
}
