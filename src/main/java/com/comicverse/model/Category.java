package com.comicverse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_name", columnNames = "name"),
    @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
})
public class Category {

    public enum Status {
        VISIBLE, // Hiển thị
        HIDDEN   // Ẩn
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority")
    private Integer priority = 0; // Số càng lớn càng ưu tiên hiển thị đầu

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.VISIBLE;

    /* ================= LIFECYCLE ================= */
    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = Status.VISIBLE;
        if (this.priority == null) this.priority = 0;
        
        // Tự động tạo slug nếu chưa có (Fallback đơn giản)
        if (this.slug == null && this.name != null) {
            this.slug = this.name.toLowerCase().replace(" ", "-");
        }
    }

    /* ================= GETTER / SETTER ================= */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}