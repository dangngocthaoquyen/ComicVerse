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
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Hiển thị trang profile
    @GetMapping
    public String userProfile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(u -> model.addAttribute("user", u));

        return "profile";
    }

    // Upload avatar
    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file, HttpSession session) throws IOException {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && !file.isEmpty()) {
            String uploadDir = "uploads/avatars/";
            new File(uploadDir).mkdirs();
            String fileName = username + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            User user = userOpt.get();
            user.setAvatar("/" + uploadDir + fileName);
            userRepository.save(user);
            session.setAttribute("avatar", user.getAvatar());
        }

        return "redirect:/profile";
    }

    // Cập nhật thông tin (email + mật khẩu)
    @PostMapping("/update")
    public String updateProfile(@RequestParam("email") String email,
                                @RequestParam("currentPassword") String currentPassword,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Kiểm tra mật khẩu cũ
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("error", "Mật khẩu hiện tại không chính xác!");
                model.addAttribute("user", user);
                return "profile";
            }

            user.setEmail(email);
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
            userRepository.save(user);
            model.addAttribute("success", "Cập nhật thành công!");
            model.addAttribute("user", user);
        }

        return "profile";
    }
}
