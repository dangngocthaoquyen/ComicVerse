package com.comicverse.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.comicverse.model.Category;
import com.comicverse.model.Chapter;
import com.comicverse.model.Comic;
import com.comicverse.model.User; // ✅ Đã thêm import User
import com.comicverse.repository.CategoryRepository;
import com.comicverse.repository.ChapterRepository;
import com.comicverse.repository.ComicRepository;
import com.comicverse.model.Comic.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.comicverse.util.SlugUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComicService {

	private final ComicRepository comicRepository;
	private final ChapterRepository chapterRepository;
	private final CategoryRepository categoryRepository;
	private final Cloudinary cloudinary;

	public ComicService(ComicRepository comicRepository, ChapterRepository chapterRepository,
			CategoryRepository categoryRepository, Cloudinary cloudinary) {
		this.comicRepository = comicRepository;
		this.chapterRepository = chapterRepository;
		this.categoryRepository = categoryRepository;
		this.cloudinary = cloudinary;
	}

	/*
	 * ========================================== 1. TẠO TRUYỆN MỚI (CẬP NHẬT THÊM
	 * USER) ==========================================
	 */
	public Comic createComic(String title, String author, String description, List<Long> categoryIds, // Danh sách ID
																										// thể loại user
																										// chọn
			MultipartFile coverFile, User uploader) throws IOException { // ✅ Thêm tham số uploader

		Comic comic = new Comic();
		comic.setTitle(title);
		comic.setSlug(SlugUtils.toSlug(title));
		comic.setAuthor(author);
		comic.setDescription(description);
		comic.setStatus(Comic.Status.ONGOING);
		comic.setUploader(uploader); // ✅ Lưu người đăng vào database

		// 1. Xử lý Categories
		if (categoryIds != null && !categoryIds.isEmpty()) {
			List<Category> categories = categoryRepository.findAllById(categoryIds);
			comic.setCategories(new HashSet<>(categories));
		}

		// 2. Upload ảnh bìa lên Cloudinary (nếu có)
		if (coverFile != null && !coverFile.isEmpty()) {
			String coverUrl = uploadToCloudinary(coverFile, "comicverse/covers");
			comic.setCoverImage(coverUrl);
		}

		return comicRepository.save(comic);
	}

	/*
	 * ========================================== 2. THÊM CHƯƠNG MỚI (CÓ SẮP XẾP
	 * ẢNH) ==========================================
	 */
	public Chapter addChapter(Long comicId, Double chapterNumber, String chapterName, List<MultipartFile> imageFiles)
			throws IOException {

		Comic comic = comicRepository.findById(comicId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy truyện"));

		// 🔥 LOGIC SẮP XẾP: Sắp xếp file theo tên (01.jpg -> 02.jpg -> ...)
		// Giúp bạn chỉ cần chọn tất cả, code sẽ tự xếp đúng thứ tự
		imageFiles.sort((f1, f2) -> {
			String name1 = f1.getOriginalFilename();
			String name2 = f2.getOriginalFilename();
			return name1.compareTo(name2);
		});

		Chapter chapter = new Chapter();
		chapter.setChapterNumber(chapterNumber);
		chapter.setChapterName(chapterName); // Có thể null
		chapter.setComic(comic);

		// Upload danh sách ảnh lên Cloudinary
		List<String> imageUrls = new ArrayList<>();
		String folderPath = "comicverse/comics/" + comicId + "/chap_" + chapterNumber;

		// Chạy song song (Parallel) để upload nhanh hơn nếu nhiều ảnh
		for (MultipartFile file : imageFiles) {
			if (!file.isEmpty()) {
				String url = uploadToCloudinary(file, folderPath);
				imageUrls.add(url);
			}
		}

		// Lưu vào DB dạng chuỗi: "link1,link2,link3"
		String content = String.join(",", imageUrls);
		chapter.setContent(content);

		// Update thời gian truyện
		comic.setUpdatedAt(java.time.LocalDateTime.now());
		comicRepository.save(comic);

		return chapterRepository.save(chapter);
	}

	/*
	 * ========================================== HELPER: UPLOAD CLOUDINARY - GIỮ
	 * NGUYÊN ==========================================
	 */
	private String uploadToCloudinary(MultipartFile file, String folder) throws IOException {
		Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
				ObjectUtils.asMap("folder", folder, "resource_type", "auto" // tự động nhận diện ảnh/video
				));
		return uploadResult.get("secure_url").toString();
	}

	/*
	 * ========================================== 3. CẬP NHẬT TRUYỆN (UPDATE)
	 * ==========================================
	 */
	public void updateComic(Long comicId, String title, String author, String description, String statusStr, List<Long> categoryIds,
			MultipartFile coverFile, User currentUser) throws IOException {

		Comic comic = comicRepository.findById(comicId).orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));

		// Check quyền: Chỉ chủ sở hữu hoặc Admin mới được sửa
		if (!comic.getUploader().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
			throw new RuntimeException("Bạn không có quyền sửa truyện này");
		}

		// Cập nhật thông tin cơ bản
		comic.setTitle(title);
		comic.setSlug(SlugUtils.toSlug(title));
		comic.setAuthor(author);
		comic.setDescription(description);
		if (statusStr != null && !statusStr.isEmpty()) {
            comic.setStatus(Status.valueOf(statusStr));
        }
		comic.setUpdatedAt(java.time.LocalDateTime.now());

		// Cập nhật thể loại (Nếu có chọn)
		if (categoryIds != null) {
			List<Category> categories = categoryRepository.findAllById(categoryIds);
			comic.setCategories(new HashSet<>(categories));
		}

		// Cập nhật ảnh bìa (CHỈ KHI NGƯỜI DÙNG UP ẢNH MỚI)
		if (coverFile != null && !coverFile.isEmpty()) {
			// Xóa ảnh cũ trên Cloudinary nếu cần (ở đây ta cứ up đè ảnh mới trước)
			String coverUrl = uploadToCloudinary(coverFile, "comicverse/covers");
			comic.setCoverImage(coverUrl);
		}

		comicRepository.save(comic);
	}

	/*
	 * ========================================== 4. HÀM LƯU ĐƠN GIẢN (Dùng để tăng
	 * view, v.v...) ==========================================
	 */
	public void save(Comic comic) {
		comicRepository.save(comic);
	}

	/*
	 * ========================================== CÁC HÀM GET DỮ LIỆU CƠ BẢN - GIỮ
	 * NGUYÊN ==========================================
	 */
	@Transactional(readOnly = true)
	public List<Comic> getAllComics() {
		return comicRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Comic getComicById(Long id) {
		return comicRepository.findById(id).orElseThrow(() -> new RuntimeException("Truyện không tồn tại!"));
	}

	@Transactional(readOnly = true)
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	// Hàm lấy danh sách truyện của user
	@Transactional(readOnly = true)
	public List<Comic> getComicsByUploader(Long uploaderId) {
		return comicRepository.findByUploaderIdOrderByUpdatedAtDesc(uploaderId);
	}
	
}