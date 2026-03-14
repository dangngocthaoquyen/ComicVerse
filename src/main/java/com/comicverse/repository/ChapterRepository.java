package com.comicverse.repository;

import com.comicverse.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Nên thêm annotation này
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    // 1. Sắp xếp TĂNG DẦN (Asc) -> Dùng cho người đọc (Đọc từ Chap 1 -> Chap 10)
    List<Chapter> findByComicIdOrderByChapterNumberAsc(Long comicId);

    // 2. Sắp xếp GIẢM DẦN (Desc) -> Dùng cho Admin/Quản lý (Để thấy Chap mới nhất vừa đăng)
    List<Chapter> findByComicIdOrderByChapterNumberDesc(Long comicId);
    
    Chapter findByComicIdAndChapterNumber(Long comicId, Double chapterNumber);
}