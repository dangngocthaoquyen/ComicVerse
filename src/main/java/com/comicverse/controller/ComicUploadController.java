package com.comicverse.controller;

import com.comicverse.model.Category; // ✅ Mới thêm: Để dùng Category::getId
import com.comicverse.model.Comic;
import com.comicverse.model.User;
import com.comicverse.repository.CategoryRepository;
import com.comicverse.service.ComicService;
import com.comicverse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors; // ✅ Mới thêm: Để xử lý danh sách ID

@Controller
@RequestMapping("/comic")
public class ComicUploadController {

	@Autowired
	private UserService userService;

	@Autowired
	private ComicService comicService;

	@Autowired
	private CategoryRepository categoryRepository;

	/*
	 * ========================================== 1. TRANG QUẢN LÝ (Danh sách truyện
	 * của tôi) ==========================================
	 */
	@GetMapping("/manage")
	public String showMyComics(HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		User currentUser = userService.getUserById(sessionUser.getId());

		// Kiểm tra quyền (Admin hoặc có quyền upload)
		if (!Boolean.TRUE.equals(currentUser.getCanUpload()) && currentUser.getRole() != User.Role.ADMIN) {
			return "redirect:/home?error=no_permission";
		}

		List<Comic> myComics = comicService.getComicsByUploader(currentUser.getId());
		model.addAttribute("myComics", myComics);

		return "comic-manage";
	}

	/*
	 * ========================================== 2. TRANG FORM TẠO MỚI (GET)
	 * ==========================================
	 */
	@GetMapping("/create")
	public String showCreateForm(HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		User currentUser = userService.getUserById(sessionUser.getId());

		if (!Boolean.TRUE.equals(currentUser.getCanUpload()) && currentUser.getRole() != User.Role.ADMIN) {
			return "redirect:/home";
		}

		// Lấy danh sách thể loại từ DB gửi sang HTML
		model.addAttribute("categories", categoryRepository.findAll());

		return "comic-create";
	}

	/*
	 * ========================================== 3. XỬ LÝ TẠO MỚI (POST)
	 * ==========================================
	 */
	@PostMapping("/create")
	public String handleCreateComic(@RequestParam("title") String title, @RequestParam("author") String author,
			@RequestParam("description") String description, @RequestParam("cover") MultipartFile cover,
			@RequestParam(value = "categoryIds", required = false) List<Long> categoryIds, HttpSession session) {

		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		try {
			User uploader = userService.getUserById(sessionUser.getId());

			comicService.createComic(title, author, description, categoryIds, cover, uploader);

			return "redirect:/comic/manage";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/comic/create?error=upload_failed";
		}
	}

	/*
	 * ========================================== 4. HIỂN THỊ FORM SỬA (GET) - MỚI
	 * ==========================================
	 */
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		// Lấy truyện từ DB
		Comic comic = comicService.getComicById(id);

		// Check quyền: Phải là người đăng hoặc Admin
		if (!comic.getUploader().getId().equals(sessionUser.getId()) && sessionUser.getRole() != User.Role.ADMIN) {
			return "redirect:/comic/manage?error=no_permission";
		}

		// Gửi thông tin truyện sang View
		model.addAttribute("comic", comic);

		// Gửi danh sách tất cả thể loại (để hiển thị checkbox)
		model.addAttribute("categories", categoryRepository.findAll());

		// 🔥 Gửi danh sách ID các thể loại ĐANG CÓ của truyện (để tick sẵn checkbox)
		List<Long> selectedCatIds = comic.getCategories().stream().map(Category::getId).collect(Collectors.toList());
		model.addAttribute("selectedCatIds", selectedCatIds);

		return "comic-create"; // Tái sử dụng giao diện tạo mới
	}

	/*
	 * ========================================== 5. XỬ LÝ CẬP NHẬT (POST) - MỚI
	 * ==========================================
	 */
	@PostMapping("/update/{id}")
	public String handleUpdateComic(@PathVariable Long id, @RequestParam("title") String title,
			@RequestParam("author") String author, @RequestParam("description") String description,
			@RequestParam("status") String status,
			@RequestParam(value = "cover", required = false) MultipartFile cover, // Không bắt buộc
			@RequestParam(value = "categoryIds", required = false) List<Long> categoryIds, HttpSession session) {

		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		try {
            User currentUser = userService.getUserById(sessionUser.getId());

            // ✅ GỌI SERVICE KÈM STATUS
            comicService.updateComic(id, title, author, description, status, categoryIds, cover, currentUser);

            return "redirect:/comic/manage";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/comic/edit/" + id + "?error=update_failed";
		}
	}

	/*
	 * ========================================== 6. HIỂN THỊ FORM UP CHAP (GET)
	 * ==========================================
	 */
	@GetMapping("/{id}/upload-chapter")
	public String showUploadChapterForm(@PathVariable Long id, HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		Comic comic = comicService.getComicById(id);

		// Check quyền: Chỉ chủ truyện hoặc Admin
		if (!comic.getUploader().getId().equals(sessionUser.getId()) && sessionUser.getRole() != User.Role.ADMIN) {
			return "redirect:/comic/manage?error=no_permission";
		}

		model.addAttribute("comic", comic);
		return "chapter-upload";
	}

	/*
	 * ========================================== 7. XỬ LÝ UP CHAP (POST)
	 * ==========================================
	 */
	@PostMapping("/{id}/upload-chapter")
	public String handleUploadChapter(@PathVariable Long id, @RequestParam("chapterNumber") Double chapterNumber,
			@RequestParam(value = "chapterName", required = false) String chapterName,
			@RequestParam("images") List<MultipartFile> images, // Nhận list ảnh
			HttpSession session) {

		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser == null)
			return "redirect:/login";

		try {
			// Check quyền lần nữa cho chắc
			Comic comic = comicService.getComicById(id);
			if (!comic.getUploader().getId().equals(sessionUser.getId()) && sessionUser.getRole() != User.Role.ADMIN) {
				return "redirect:/home";
			}

			comicService.addChapter(id, chapterNumber, chapterName, images);

			return "redirect:/comic/manage?success=true";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/comic/" + id + "/upload-chapter?error=upload_failed";
		}
	}
}