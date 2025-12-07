package com.comicverse.controller;

import com.comicverse.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminOtpController {

    @Autowired
    private OtpService otpService;

    @GetMapping("/verify-otp")
    public String otpPage() {
        return "admin/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam("otp") String otp,
            HttpSession session,
            Model model
    ) {

        String email = (String) session.getAttribute("pendingAdminEmail");

        if (email == null) {
            return "redirect:/login";
        }

        if (!otpService.verifyOtp(email, otp)) {
            model.addAttribute("error", "OTP không hợp lệ hoặc đã hết hạn!");
            return "admin/verify-otp";
        }

        session.setAttribute("adminVerified", true);

        return "redirect:/admin/dashboard";
    }
}
