package com.comicverse.controller;

import com.comicverse.model.User;
import com.comicverse.repository.UserRepository;
import com.comicverse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AdminUserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /* =======================
       GET /admin/users
       (Giống doGet() servlet: load list users)
       ======================= */
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users"; // templates/admin/users.html
    }

    /* =======================
       POST /admin/users/{id}/lock
       (Giống action=deactivate)
       ======================= */
    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable Long id, HttpSession session) {

        // Chặn admin tự khóa mình (giống servlet)
        if (isCurrentUser(session, id)) {
            session.setAttribute("error", "Bạn không thể khóa tài khoản của chính mình!");
            return "redirect:/admin/users";
        }

        userService.lockUser(id);
        session.setAttribute("success", "Đã khóa tài khoản thành công!");
        return "redirect:/admin/users";
    }

    /* =======================
       POST /admin/users/{id}/unlock
       (Giống action=activate)
       ======================= */
    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id, HttpSession session) {

        if (isCurrentUser(session, id)) {
            session.setAttribute("error", "Bạn không thể thao tác trên chính mình!");
            return "redirect:/admin/users";
        }

        userService.unlockUser(id);
        session.setAttribute("success", "Đã mở khóa tài khoản thành công!");
        return "redirect:/admin/users";
    }

    /* =======================
       POST /admin/users/{id}/toggle-upload
       (Mới: bật/tắt can_upload)
       ======================= */
    @PostMapping("/{id}/toggle-upload")
    public String toggleUpload(@PathVariable Long id, HttpSession session) {

        if (isCurrentUser(session, id)) {
            session.setAttribute("error", "Bạn không thể tự bật/tắt quyền upload của chính mình!");
            return "redirect:/admin/users";
        }

        userService.toggleUpload(id);
        session.setAttribute("success", "Đã cập nhật quyền upload thành công!");
        return "redirect:/admin/users";
    }

    /* =======================
       Helper: check current login user
       Bạn đang lưu user trong session: session.setAttribute("user", user)
       ======================= */
    private boolean isCurrentUser(HttpSession session, Long targetUserId) {
        Object obj = session.getAttribute("user");
        if (!(obj instanceof User currentUser)) return false;
        return currentUser.getId() != null && currentUser.getId().equals(targetUserId);
    }
}
