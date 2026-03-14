package com.comicverse.controller;

import com.comicverse.model.Chapter;
import com.comicverse.model.Comment;
import com.comicverse.model.User;
import com.comicverse.repository.ChapterRepository;
import com.comicverse.repository.CommentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // ✅ Import này bị thiếu
import org.springframework.data.domain.Sort;        // ✅ Import này bị thiếu
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;                // ✅ Import này bị thiếu
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private ChapterRepository chapterRepository;

    /* ==========================================
       1. XỬ LÝ GỬI BÌNH LUẬN (POST)
       ========================================== */
    @PostMapping("/comment/add")
    public String addComment(@RequestParam Long chapterId,
                             @RequestParam String content,
                             @RequestParam(required = false) Long parentId, // ID bình luận cha (nếu có)
                             HttpSession session,
                             HttpServletRequest request) {
        
        // 1. Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // Lưu lại trang hiện tại để login xong quay lại (nếu cần xử lý kỹ hơn ở SecurityConfig)
            return "redirect:/login";
        }

        // 2. Lấy thông tin Chapter
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter không tồn tại"));

        // 3. Tạo Comment mới
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setChapter(chapter);
        comment.setComic(chapter.getComic()); // ✅ Tự động lấy Comic từ Chapter

        // 4. Xử lý Reply (Nếu là trả lời bình luận khác)
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElse(null);
            comment.setParentComment(parent);
        }

        // 5. Lưu vào DB
        commentRepository.save(comment);

        // 6. Quay lại trang cũ (Refresh trang)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/home");
    }
    
    /* ==========================================
       2. TẢI THÊM BÌNH LUẬN (AJAX LOAD MORE)
       ========================================== */
    @GetMapping("/comment/load-more")
    public String loadMoreComments(@RequestParam(required = false) Long chapterId,
                                   @RequestParam(required = false) Long comicId, // ✅ Thêm cái này
                                   @RequestParam(defaultValue = "0") int page, 
                                   Model model) {
        
        var pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Comment> commentPage;

        if (chapterId != null) {
            commentPage = commentRepository.findByChapterIdAndParentCommentIsNull(chapterId, pageable);
        } else if (comicId != null) {
            // ✅ Logic load cho trang chi tiết
            commentPage = commentRepository.findByComicIdAndParentCommentIsNull(comicId, pageable);
        } else {
            return "";
        }
        
        model.addAttribute("comments", commentPage.getContent());
        
        // Trả về đúng fragment (lưu ý tên file html bên dưới phải khớp)
        return "comic-detail :: commentListFragment"; 
    }
}