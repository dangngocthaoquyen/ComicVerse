package com.comicverse.service;

import com.comicverse.model.User;
import com.comicverse.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection (Eclipse không cần Lombok)
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================
       AUTH / USER FUNCTIONS
       ========================= */

    // REGISTER
    public void register(User user) {

        // Trim cơ bản (tránh email có khoảng trắng)
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }
        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Mặc định USER
        user.setRole(User.Role.USER);

        // Mặc định ACTIVE nếu null
        if (user.getStatus() == null) {
            user.setStatus(User.Status.ACTIVE);
        }

        // Mặc định false nếu null
        if (user.getCanUpload() == null) {
            user.setCanUpload(false);
        }

        // Mặc định điểm = 0 nếu null
        if (user.getPoints() == null) {
            user.setPoints(0);
        }

        userRepository.save(user);
    }

    // FIND BY EMAIL
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email.trim());
    }

    /* =========================
       ADMIN FUNCTIONS
       ========================= */

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id = " + userId));
    }

    public void lockUser(Long userId) {
        User user = getUserById(userId);

        // an toàn: không khóa ADMIN (tuỳ bạn)
        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Không thể khóa tài khoản ADMIN.");
        }

        user.setStatus(User.Status.LOCKED);
        userRepository.save(user);
    }

    public void unlockUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
    }

    // Bật/Tắt quyền upload (can_upload)
    public void toggleUpload(Long userId) {
        User user = getUserById(userId);


        boolean current = Boolean.TRUE.equals(user.getCanUpload());
        user.setCanUpload(!current);
        userRepository.save(user);
    }

    public void setUploadPermission(Long userId, boolean canUpload) {
        User user = getUserById(userId);

        user.setCanUpload(canUpload);
        

        userRepository.save(user);
    }

    // (gợi ý thêm) cộng điểm cho user
    public void addPoints(Long userId, int amount) {
        User user = getUserById(userId);
        
        // 1. Cộng điểm an toàn (tránh null)
        int currentPoints = (user.getPoints() == null) ? 0 : user.getPoints();
        int newPoints = currentPoints + amount;
        user.setPoints(newPoints);

        // 2. LOGIC TỰ ĐỘNG CẤP QUYỀN (Auto-promote)
        // Nếu điểm >= 1000 VÀ chưa có quyền upload -> Cấp quyền luôn
        if (newPoints >= 1000 && !Boolean.TRUE.equals(user.getCanUpload())) {
            user.setCanUpload(true);
            // (Optional) Có thể gửi mail thông báo chúc mừng ở đây
        }

        userRepository.save(user);
    }
}
