package com.comicverse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
public class Chapter {

    /* ==============================================
       1. CÁC THUỘC TÍNH CƠ BẢN (BASIC FIELDS)
       ============================================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_number", nullable = false)
    private Double chapterNumber; // Hỗ trợ chương 1.5, 2.5

    @Column(name = "chapter_name")
    private String chapterName; 

    // Sử dụng LONGTEXT để lưu được nhiều link ảnh hơn (TEXT thường chỉ ~64KB)
    @Column(columnDefinition = "LONGTEXT") 
    private String content; 

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* ==============================================
       2. QUẢN LÝ TRẠNG THÁI (STATUS FIELDS - MỚI)
       ============================================== */

    // Cờ đánh dấu chương VIP/Khóa (True = Phải đăng nhập mới đọc được)
    @Column(name = "is_locked")
    private Boolean isLocked = false; // Mặc định là False (Đọc miễn phí)

    // Cờ đánh dấu duyệt/ẩn (True = Được hiện, False = Bị ẩn/Nháp)
    @Column(name = "is_active")
    private Boolean isActive = true;  // Mặc định là True (Hiện ngay khi đăng)

    /* ==============================================
       3. CÁC MỐI QUAN HỆ (RELATIONSHIPS)
       ============================================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;

    // Liên kết với Comment: Khi xóa Chapter -> Xóa sạch bình luận của Chapter đó
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /* ==============================================
       4. LIFECYCLE (AUTO GENERATE DATA)
       ============================================== */

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        
        // Khởi tạo giá trị mặc định nếu null
        if (this.viewCount == null) this.viewCount = 0L;
        if (this.isLocked == null) this.isLocked = false;
        if (this.isActive == null) this.isActive = true;
    }

    /* ==============================================
       5. GETTER & SETTER
       ============================================== */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Double getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(Double chapterNumber) { this.chapterNumber = chapterNumber; }
    
    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    
    public Comic getComic() { return comic; }
    public void setComic(Comic comic) { this.comic = comic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    // --- Getter & Setter cho các trường mới ---

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean locked) { isLocked = locked; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}