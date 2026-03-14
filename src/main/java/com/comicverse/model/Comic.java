package com.comicverse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "comics")
public class Comic {

	public enum Status {
		ONGOING, // Đang tiến hành
		COMPLETED, // Đã hoàn thành
		DROPPED // Tạm ngưng
	}

	/*
	 * ============================================== 1. CÁC THUỘC TÍNH CƠ BẢN
	 * (FIELDS) ==============================================
	 */

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String author;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "cover_image")
	private String coverImage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status = Status.ONGOING;

	/*
	 * ============================================== 2. THỐNG KÊ (STATS)
	 * ==============================================
	 */

	@Column(name = "view_count")
	private Long viewCount = 0L;

	@Column(name = "follow_count")
	private Integer followCount = 0;

	@Column(name = "rating_average")
	private Double ratingAverage = 0.0; // Điểm trung bình (VD: 4.5)

	@Column(name = "rating_count")
	private Integer ratingCount = 0; // Số lượng người đã đánh giá

	/*
	 * ============================================== 3. THỜI GIAN (TIMESTAMPS)
	 * ==============================================
	 */

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/*
	 * ============================================== 4. CÁC MỐI QUAN HỆ
	 * (RELATIONSHIPS) ==============================================
	 */

	// A. Người đăng truyện (ManyToOne)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploader_id", nullable = false)
	private User uploader;

	// B. Thể loại (ManyToMany)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "comic_category", joinColumns = @JoinColumn(name = "comic_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new HashSet<>();

	// C. Danh sách Chương (OneToMany)
	@OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Chapter> chapters = new ArrayList<>();

	// D. Danh sách Bình luận (OneToMany)
	@OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();

	// ✅ E. Danh sách Review (OneToMany - MỚI THÊM)
	@OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();

	// F. Danh sách Người theo dõi (ManyToMany)
	@ManyToMany(mappedBy = "followedComics", fetch = FetchType.LAZY)
	private Set<User> followers = new HashSet<>();

	@Column(name = "media_url")
	private String mediaUrl;

	// ✅ THÊM: Cột slug (unique để không trùng nhau)
	@Column(unique = true, nullable = false)
	private String slug;

	/*
	 * ============================================== 5. LIFECYCLE (AUTO GENERATE
	 * DATA) ==============================================
	 */

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;

		// Init giá trị mặc định tránh null
		if (this.status == null)
			this.status = Status.ONGOING;
		if (this.viewCount == null)
			this.viewCount = 0L;

		// Init các trường thống kê mới
		if (this.followCount == null)
			this.followCount = 0;
		if (this.ratingAverage == null)
			this.ratingAverage = 0.0;
		if (this.ratingCount == null)
			this.ratingCount = 0;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/*
	 * ============================================== 6. HELPER METHODS
	 * ==============================================
	 */

	public boolean isFinished() {
		return this.status == Status.COMPLETED;
	}

	/*
	 * ============================================== HELPER METHOD CHO VIEW
	 * (THYMELEAF) ==============================================
	 */

	// Hàm này giúp lấy số chương mới nhất để hiện ra ngoài trang chủ
	public String getLatestChapterNum() {
		if (this.chapters == null || this.chapters.isEmpty()) {
			return "0";
		}

		// Tìm chapter có số lớn nhất
		Chapter latest = this.chapters.stream()
				.max((c1, c2) -> Double.compare(c1.getChapterNumber(), c2.getChapterNumber())).orElse(null);

		if (latest == null)
			return "0";

		// Xử lý hiển thị: Nếu là 24.0 thì hiện "24", nếu 24.5 thì hiện "24.5"
		double num = latest.getChapterNumber();
		if (num == (long) num) {
			return String.valueOf((long) num);
		} else {
			return String.valueOf(num);
		}
	}

	/*
	 * ============================================== 7. GETTER / SETTER
	 * ==============================================
	 */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCoverImage() {
		return coverImage;
	}

	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	// Stats
	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	public Integer getFollowCount() {
		return followCount;
	}

	public void setFollowCount(Integer followCount) {
		this.followCount = followCount;
	}

	public Double getRatingAverage() {
		return ratingAverage;
	}

	public void setRatingAverage(Double ratingAverage) {
		this.ratingAverage = ratingAverage;
	}

	public Integer getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}

	// Dates
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// Relationships
	public User getUploader() {
		return uploader;
	}

	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	// ✅ Getter Setter cho Reviews (Mới)
	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public Set<User> getFollowers() {
		return followers;
	}

	public void setFollowers(Set<User> followers) {
		this.followers = followers;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}
}