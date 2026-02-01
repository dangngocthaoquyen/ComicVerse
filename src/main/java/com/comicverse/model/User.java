package com.comicverse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    // ADMIN / USER
    private String role;

    @Column(name = "avatar")
    private String avatar;

    // poster / uploader
    @Column(name = "can_upload")
    private Boolean canUpload = false;

    // ACTIVE / LOCKED
    @Column(name = "status")
    private String status = "ACTIVE";

    /* ================= GETTER / SETTER ================= */

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean getCanUpload() {
        return canUpload;
    }

    public String getStatus() {
        return status;
    }

    /* ================= LOGIC TIỆN DÙNG ================= */

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public boolean isLocked() {
        return "LOCKED".equalsIgnoreCase(this.status);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isUploader() {
        return Boolean.TRUE.equals(this.canUpload);
    }

    /* ================= SET ================= */

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setCanUpload(Boolean canUpload) {
        this.canUpload = canUpload;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
