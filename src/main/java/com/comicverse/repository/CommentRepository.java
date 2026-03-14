package com.comicverse.repository;

import com.comicverse.model.Comment;
import org.springframework.data.domain.Page; // ✅ Import Page
import org.springframework.data.domain.Pageable; // ✅ Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // ✅ CŨ (Bỏ hoặc giữ lại nếu cần dùng chỗ khác):
    // List<Comment> findByChapterIdAndParentCommentIsNullOrderByCreatedAtDesc(Long chapterId);
	List<Comment> findByComicIdOrderByCreatedAtDesc(Long comicId);

    // ✅ MỚI: Hỗ trợ phân trang cho comment gốc (parent is null)
    Page<Comment> findByChapterIdAndParentCommentIsNull(Long chapterId, Pageable pageable);
    
 // Lấy comment gốc của 1 TRUYỆN (bất kể chapter nào), có phân trang
    Page<Comment> findByComicIdAndParentCommentIsNull(Long comicId, Pageable pageable);

    // Đếm tổng comment của truyện
    int countByComicId(Long comicId);

    int countByChapterId(Long chapterId);
}