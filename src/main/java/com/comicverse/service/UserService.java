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

    // ===== STATUS CONSTANTS =====
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    // Constructor injection (Eclipse không cần Lombok vẫn OK)
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================
       AUTH / USER FUNCTIONS (cũ)
       ========================= */

    // REGISTER
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        // nếu bạn vừa thêm cột status thì set mặc định luôn
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus(STATUS_ACTIVE);
        }

        // nếu null thì set mặc định false (an toàn)
        if (user.getCanUpload() == null) {
            user.setCanUpload(false);
        }

        userRepository.save(user);
    }

    // FIND BY EMAIL
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /* =========================
       ADMIN FUNCTIONS (mới)
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
        user.setStatus(STATUS_LOCKED);
        userRepository.save(user);
    }

    public void unlockUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus(STATUS_ACTIVE);
        userRepository.save(user);
    }

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
}
