package com.comicverse.repository;

import com.comicverse.model.Comic;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {

    /* =========================================================
       1. TÌM KIẾM CƠ BẢN
       ========================================================= */
    
    // Tìm theo Slug (tránh NullPointer)
    Optional<Comic> findBySlug(String slug);

    // Tìm theo Title (chứa từ khóa, không phân biệt hoa thường)
    List<Comic> findByTitleContainingIgnoreCase(String keyword);

    // Tìm theo Trạng thái (Ongoing/Completed)
    List<Comic> findByStatus(Comic.Status status);

    // Tìm truyện theo người đăng (Sắp xếp theo ngày update giảm dần)
    List<Comic> findByUploaderIdOrderByUpdatedAtDesc(Long uploaderId);


    /* =========================================================
       2. LỌC THEO THỂ LOẠI (CATEGORY)
       ========================================================= */

    // Phiên bản cũ (Chỉ tìm list)
    List<Comic> findByCategories_Slug(String categorySlug);

    // ✅ Phiên bản MỚI (Hỗ trợ SẮP XẾP - Sort)
    // Dùng cho: /category/{slug}?sort=view
    List<Comic> findByCategories_Slug(String categorySlug, Sort sort);


    /* =========================================================
       3. TÌM KIẾM NÂNG CAO (LIVE SEARCH & SEARCH PAGE)
       ========================================================= */

    // Query chung cho tìm kiếm: Tìm trong Title HOẶC Slug
    String SEARCH_QUERY = "SELECT c FROM Comic c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.slug) LIKE LOWER(CONCAT('%', :slugKeyword, '%'))";

    // ✅ Phiên bản 1: Không có Sort (Dùng cho API Live Search gợi ý nhanh)
    @Query(SEARCH_QUERY)
    List<Comic> searchComics(@Param("keyword") String keyword, 
                             @Param("slugKeyword") String slugKeyword);

    // ✅ Phiên bản 2: Có Sort (Dùng cho trang kết quả tìm kiếm đầy đủ)
    @Query(SEARCH_QUERY)
    List<Comic> searchComics(@Param("keyword") String keyword, 
                             @Param("slugKeyword") String slugKeyword, 
                             Sort sort);
}