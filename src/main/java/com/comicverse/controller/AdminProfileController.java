package com.comicverse.controller;

import com.comicverse.model.User;
import com.comicverse.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 📌 Hiển thị trang profile
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {

        User admin = (User) session.getAttribute("user");

        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("admin", admin);
        return "admin/admin-profile";
    }

    // 📌 Đổi avatar
    @PostMapping("/profile/avatar")
    public String updateAvatar(
            @RequestParam("avatarFile") MultipartFile file,
            HttpSession session
    ) throws IOException {

        User admin = (User) session.getAttribute("user");

        if (admin == null) return "redirect:/login";

        if (!file.isEmpty()) {
            String uploadDir = "uploads/";

            File dest = new File(uploadDir + file.getOriginalFilename());
            file.transferTo(dest);

            admin.setAvatar("/uploads/" + file.getOriginalFilename());
            userRepository.save(admin);

            session.setAttribute("user", admin);
        }

        return "redirect:/admin/profile";
    }

    // 📌 Update email + password
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("email") String email,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            HttpSession session,
            Model model
    ) {

        User admin = (User) session.getAttribute("user");

        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng!");
            model.addAttribute("admin", admin);
            return "admin/admin-profile";
        }

        admin.setEmail(email);

        if (newPassword != null && !newPassword.isBlank()) {
            admin.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(admin);
        session.setAttribute("user", admin);

        model.addAttribute("success", "Cập nhật thành công!");
        model.addAttribute("admin", admin);

        return "admin/admin-profile";
    }
}
