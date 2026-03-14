package com.comicverse.controller;

import com.comicverse.model.User;
import com.comicverse.service.FollowService;
import com.comicverse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FollowController {

    @Autowired private FollowService followService;
    @Autowired private UserService userService;

    // 1. Xử lý bấm nút theo dõi (Dùng POST để an toàn)
    @PostMapping("/follow/{comicId}")
    public String followComic(@PathVariable Long comicId, HttpSession session, @RequestHeader(value = "referer", required = false) String referer) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        followService.toggleFollow(sessionUser.getId(), comicId);
        
        // Quay lại trang trước đó (chi tiết truyện hoặc chương đang đọc)
        return "redirect:" + (referer != null ? referer : "/home");
    }

    // 2. Trang danh sách theo dõi của tôi
    @GetMapping("/theo-doi")
    public String myFollowedComics(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Lấy lại user từ DB để có danh sách truyện mới nhất (tránh lỗi Lazy Loading)
        User currentUser = userService.getUserById(sessionUser.getId());
        
        model.addAttribute("followedComics", currentUser.getFollowedComics());
        model.addAttribute("pageTitle", "Truyện đang theo dõi");
        return "followed-list"; // Chúng ta sẽ tạo file này ở bước 4
    }
}