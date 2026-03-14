package com.comicverse.controller;

import com.comicverse.model.Chapter;
import com.comicverse.model.Comic;
import com.comicverse.model.Comment;
import com.comicverse.model.User;
import com.comicverse.repository.CategoryRepository;
import com.comicverse.repository.ChapterRepository;
import com.comicverse.repository.ComicRepository;
import com.comicverse.repository.CommentRepository;
import com.comicverse.service.ComicService;
import com.comicverse.service.UserService;
import com.comicverse.util.SlugUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class ComicController {

    @Autowired private ComicService comicService;
    @Autowired private ComicRepository comicRepository;
    @Autowired private ChapterRepository chapterRepository;
    @Autowired private UserService userService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CommentRepository commentRepository;

    /* ==========================================
       HÀM HỖ TRỢ: TẠO ĐỐI TƯỢNG SORT
       ========================================== */
    private Sort getSort(String sortType) {
        if ("view".equals(sortType)) {
            return Sort.by(Sort.Direction.DESC, "viewCount"); // Xem nhiều nhất
        } else if ("rating".equals(sortType)) {
            return Sort.by(Sort.Direction.DESC, "ratingAverage"); // Đánh giá cao nhất
        } else {
            return Sort.by(Sort.Direction.DESC, "updatedAt"); // Mới cập nhật (Mặc định)
        }
    }

    /* ==========================================
       1. TRANG CHỦ (HIỂN THỊ TẤT CẢ + SORT)
       URL: /home?sort=view
       ========================================== */
    @GetMapping({"/", "/home"})
    public String viewHome(Model model, 
                           @RequestParam(required = false, defaultValue = "latest") String sort) {
        
        // Lấy danh sách truyện có sắp xếp
        List<Comic> comics = comicRepository.findAll(getSort(sort));
        
        model.addAttribute("latestComics", comics);
        model.addAttribute("pageTitle", "Truyện mới cập nhật"); // Tiêu đề mặc định
        model.addAttribute("currentSort", sort); // Để highlight nút đang chọn
        model.addAttribute("baseUrl", "/home"); // Để nút bấm biết đường dẫn gốc
        
        // Load sidebar danh mục
        model.addAttribute("categories", categoryRepository.findAll());
        return "home";
    }

    /* ==========================================
       ✅ ĐƯỜNG DẪN /upload
       Chuyển hướng về trang tạo truyện (/comic/create)
       ========================================== */
    @GetMapping("/upload")
    public String redirectToUpload() {
        return "redirect:/comic/manage";
    }

    /* ==========================================
       2. LỌC THEO THỂ LOẠI (CATEGORY + SORT)
       URL: /category/{slug}?sort=rating
       ========================================== */
    @GetMapping("/category/{categorySlug}")
    public String viewCategory(@PathVariable String categorySlug, 
                               @RequestParam(required = false, defaultValue = "latest") String sort,
                               Model model) {
        
        // Gọi hàm repository tìm theo category có Sort
        List<Comic> comics = comicRepository.findByCategories_Slug(categorySlug, getSort(sort));
        
        // Lấy tên category để hiển thị tiêu đề đẹp
        var category = categoryRepository.findBySlug(categorySlug);
        String catName = category.isPresent() ? category.get().getName() : "Thể loại";

        model.addAttribute("pageTitle", "Truyện " + catName + " mới cập nhật");
        
        model.addAttribute("latestComics", comics);
        model.addAttribute("currentSort", sort);
        model.addAttribute("baseUrl", "/category/" + categorySlug); // URL gốc cho category
        
        model.addAttribute("categories", categoryRepository.findAll());
        return "home"; 
    }

    /* ==========================================
       3. TRANG TÌM KIẾM (SEARCH + SORT)
       URL: /search?keyword=abc&sort=view
       ========================================== */
    @GetMapping("/search")
    public String searchComics(@RequestParam String keyword,
                               @RequestParam(required = false, defaultValue = "latest") String sort,
                               Model model) {
        
        String slugKeyword = SlugUtils.toSlug(keyword);
        // Tìm kiếm kết hợp sắp xếp
        List<Comic> comics = comicRepository.searchComics(keyword, slugKeyword, getSort(sort));

        model.addAttribute("latestComics", comics);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm: " + keyword);
        
        model.addAttribute("currentSort", sort);
        // Với search, baseUrl phải là /search và cần kèm theo keyword
        model.addAttribute("baseUrl", "/search"); 
        model.addAttribute("currentKeyword", keyword); // Gửi lại keyword để nối vào link sort
        
        model.addAttribute("categories", categoryRepository.findAll());
        return "home";
    }
    /* ==========================================
    4. HIỂN THỊ CHI TIẾT TRUYỆN
    URL: /truyen/{slug}
    ========================================== */
 @GetMapping("/truyen/{slug}")
 public String viewComicDetail(@PathVariable String slug, 
                               @RequestParam(defaultValue = "0") int page, // ✅ SỬA 1: Thêm tham số page
                               Model model, 
                               HttpSession session) {
     
     Comic comic = comicRepository.findBySlug(slug)
             .orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));

     // Tăng view truyện
     comic.setViewCount(comic.getViewCount() + 1);
     comicService.save(comic);

     // Lấy danh sách chương
     List<Chapter> chapters = chapterRepository.findByComicIdOrderByChapterNumberDesc(comic.getId());

     // Kiểm tra Follow
     User sessionUser = (User) session.getAttribute("user");
     boolean isFollowing = false;
     if (sessionUser != null) {
         User currentUser = userService.getUserById(sessionUser.getId());
         if (currentUser.getFollowedComics() != null) {
             isFollowing = currentUser.getFollowedComics().contains(comic);
         }
     }

     // ✅ SỬA 2: Logic lấy 10 comment cha mới nhất
     // Tạo PageRequest: Trang hiện tại (page), lấy 10 cái, sắp xếp mới nhất (createdAt DESC)
     var pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
     
     // Gọi hàm Repo: Chỉ tìm comment có parent = null (comment gốc)
     Page<Comment> commentPage = commentRepository.findByComicIdAndParentCommentIsNull(comic.getId(), pageable);
     
     // Đếm tổng số lượng comment (để hiện con số tổng trên tab)
     int totalComments = commentRepository.countByComicId(comic.getId());

     // ✅ SỬA 3: Đẩy dữ liệu phân trang ra View
     model.addAttribute("comments", commentPage.getContent()); // List 10 comment
     model.addAttribute("totalPages", commentPage.getTotalPages()); // Tổng số trang (để hiện nút Xem thêm)
     model.addAttribute("totalComments", totalComments); // Tổng số lượng

     model.addAttribute("comic", comic);
     model.addAttribute("chapters", chapters);
     model.addAttribute("isFollowing", isFollowing);

     return "comic-detail";
 }

    /* ==========================================
       5. ĐỌC TRUYỆN (CẬP NHẬT VIEW + FOLLOW + NAV + COMMENT PAGING)
       ========================================== */
    @GetMapping("/truyen/{slug}/chap-{chapterNumber}")
    public String readChapter(@PathVariable String slug, 
                              @PathVariable Double chapterNumber, 
                              Model model,
                              HttpSession session,
                              HttpServletRequest request) {
        
        Comic comic = comicRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));

        Chapter currentChapter = chapterRepository.findByComicIdAndChapterNumber(comic.getId(), chapterNumber);
        if (currentChapter == null) throw new RuntimeException("Chương không tồn tại");

        // Tăng view & Lưu
        currentChapter.setViewCount(currentChapter.getViewCount() + 1);
        chapterRepository.save(currentChapter);

        // Tách ảnh
        List<String> images = new ArrayList<>();
        if (currentChapter.getContent() != null && !currentChapter.getContent().isEmpty()) {
            images = Arrays.asList(currentChapter.getContent().split(","));
        }

        // Nav Prev/Next
        List<Chapter> allChapters = chapterRepository.findByComicIdOrderByChapterNumberAsc(comic.getId());
        Double prevChapNum = null;
        Double nextChapNum = null;

        for (int i = 0; i < allChapters.size(); i++) {
            if (allChapters.get(i).getId().equals(currentChapter.getId())) {
                if (i > 0) prevChapNum = allChapters.get(i - 1).getChapterNumber();
                if (i < allChapters.size() - 1) nextChapNum = allChapters.get(i + 1).getChapterNumber();
                break;
            }
        }

        // Kiểm tra Follow
        User sessionUser = (User) session.getAttribute("user");
        boolean isFollowing = false;
        if (sessionUser != null) {
            User currentUser = userService.getUserById(sessionUser.getId());
            if (currentUser.getFollowedComics() != null) {
                isFollowing = currentUser.getFollowedComics().contains(comic);
            }
        }
        
        // ✅ Lấy comment phân trang (10 cái đầu tiên)
        var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findByChapterIdAndParentCommentIsNull(currentChapter.getId(), pageable);
        
        // Lấy tổng số lượng comment
        int totalComments = commentRepository.countByChapterId(currentChapter.getId());

        // Đẩy dữ liệu ra View
        model.addAttribute("comments", commentPage.getContent()); // Chỉ gửi 10 comment
        model.addAttribute("totalPages", commentPage.getTotalPages()); // Để hiện nút Xem thêm
        model.addAttribute("totalComments", totalComments); 
        
        model.addAttribute("chapter", currentChapter);
        model.addAttribute("images", images);
        model.addAttribute("comic", comic);
        model.addAttribute("prevChapNum", prevChapNum);
        model.addAttribute("nextChapNum", nextChapNum);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("currentUrl", request.getRequestURI());

        return "chapter-read";
    }
}