package com.comicverse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token đặt unique để không trùng lặp khi kiểm tra
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    // Email của người cần reset
    @Column(nullable = false, length = 255)
    private String email;

    // Ngày hết hạn (15 phút sau khi tạo)
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // ===== Constructors =====
    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
    }

    // ===== Getter / Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    // ===== toString() để debug =====
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
