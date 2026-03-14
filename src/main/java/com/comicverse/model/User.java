package com.comicverse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList; // Nhớ import List
import java.util.HashSet;   // Nhớ import Set
import java.util.List;
import java.util.Set;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    }
)
public class User {

    public enum Role {
        ADMIN, USER
    }

    public enum Status {
        ACTIVE, LOCKED
    }

    /* ==============================================
       1. CÁC THUỘC TÍNH CƠ BẢN (FIELDS)
       ============================================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // ADMIN / USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "avatar")
    private String avatar;

    // poster / uploader
    @Column(name = "can_upload", nullable = false)
    private Boolean canUpload = false;

    // điểm để xin quyền upload
    @Column(nullable = false)
    private Integer points = 0;

    // ACTIVE / LOCKED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ==============================================
       2. CÁC MỐI QUAN HỆ (RELATIONSHIPS) - MỚI THÊM
       ============================================== */

    // ✅ A. DANH SÁCH TRUYỆN ĐANG THEO DÕI
    // (User theo dõi nhiều truyện, Truyện được theo dõi bởi nhiều User)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_follows",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "comic_id"))
    private Set<Comic> followedComics = new HashSet<>();

    // ✅ B. DANH SÁCH BÌNH LUẬN CỦA USER
    // (User viết nhiều bình luận. mappedBy="user" trỏ về biến user bên Entity Comment)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


    /* ==============================================
       3. LIFECYCLE (AUTO GENERATE DATE)
       ============================================== */

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // đảm bảo không null
        if (this.canUpload == null) this.canUpload = false;
        if (this.points == null) this.points = 0;
        if (this.role == null) this.role = Role.USER;
        if (this.status == null) this.status = Status.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.canUpload == null) this.canUpload = false;
        if (this.points == null) this.points = 0;
    }

    /* ==============================================
       4. GETTER / SETTER
       ============================================== */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Boolean getCanUpload() { return canUpload; }
    public void setCanUpload(Boolean canUpload) { this.canUpload = canUpload; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- Getter/Setter cho 2 phần mới thêm ---

    public Set<Comic> getFollowedComics() { return followedComics; }
    public void setFollowedComics(Set<Comic> followedComics) { this.followedComics = followedComics; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }


    /* ==============================================
       5. HELPER METHODS (LOGIC TIỆN DÙNG)
       ============================================== */

    public boolean isActive() { return this.status == Status.ACTIVE; }
    public boolean isLocked() { return this.status == Status.LOCKED; }
    public boolean isAdmin() { return this.role == Role.ADMIN; }
    public boolean isUploader() { return Boolean.TRUE.equals(this.canUpload); }

    // Helper set Role từ String
    public void setRoleFromString(String role) {
        if (role == null) return;
        this.role = Role.valueOf(role.trim().toUpperCase());
    }

    // Helper set Status từ String
    public void setStatusFromString(String status) {
        if (status == null) return;
        this.status = Status.valueOf(status.trim().toUpperCase());
    }
}