package com.comicverse.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.comicverse.model.User;
import com.comicverse.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping
    public String userProfile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(u -> model.addAttribute("user", u));

        return "profile";
    }

    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file,
                               HttpSession session) throws IOException {

        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent() && !file.isEmpty()) {

            // Upload lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "comicverse/avatars",
                            "public_id", username + "_avatar",
                            "overwrite", true
                    )
            );

            String imageUrl = uploadResult.get("secure_url").toString();

            User user = userOpt.get();
            user.setAvatar(imageUrl);
            userRepository.save(user);

            session.setAttribute("avatar", imageUrl);
        }

        return "redirect:/profile";
    }


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

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("error", "Mật khẩu cũ không đúng!");
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
