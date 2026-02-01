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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private boolean checkOtp(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute("adminVerified");
        return verified != null && verified;
    }

    private User getCurrentAdmin(HttpSession session) {
        return (User) session.getAttribute("user"); // ✅ bạn đang lưu "user"
    }

    @GetMapping
    public String profilePage(HttpSession session, Model model) {
        if (!checkOtp(session)) return "redirect:/admin/verify-otp";

        User admin = getCurrentAdmin(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("admin", admin);
        return "admin/admin-profile"; // file html mới
    }

    @PostMapping("/update")
    public String updateProfile(
            HttpSession session,
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes ra
    ) {
        if (!checkOtp(session)) return "redirect:/admin/verify-otp";

        User admin = getCurrentAdmin(session);
        if (admin == null) return "redirect:/login";

        // check password hiện tại
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/admin/profile";
        }

        admin.setEmail(email);

        if (newPassword != null && !newPassword.isBlank()) {
            admin.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(admin);

        // cập nhật session (nếu bạn hiển thị avatar/username từ session)
        session.setAttribute("user", admin);
        session.setAttribute("username", admin.getUsername());
        session.setAttribute("avatar", admin.getAvatar());

        ra.addFlashAttribute("success", "Cập nhật thành công!");
        return "redirect:/admin/profile";
    }

    @PostMapping("/avatar")
    public String updateAvatar(HttpSession session,
                               @RequestParam("avatarFile") MultipartFile file,
                               RedirectAttributes ra) {
        if (!checkOtp(session)) return "redirect:/admin/verify-otp";

        User admin = getCurrentAdmin(session);
        if (admin == null) return "redirect:/login";

        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng chọn ảnh.");
            return "redirect:/admin/profile";
        }

        try {
            String uploadDir = "D:/ComicVerseUploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String original = file.getOriginalFilename() == null ? "avatar.jpg" : file.getOriginalFilename();
            String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = System.currentTimeMillis() + "_" + safe;

            File dest = new File(dir, filename);
            file.transferTo(dest);

            // ✅ QUAN TRỌNG: URL phải khớp với WebConfig (/avatars/**)
            String avatarUrl = "/avatars/" + filename;

            admin.setAvatar(avatarUrl);
            userRepository.save(admin);

            session.setAttribute("avatar", avatarUrl);
            ra.addFlashAttribute("success", "Đổi avatar thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Upload thất bại: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

}
