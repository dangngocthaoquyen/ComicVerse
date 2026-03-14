package com.comicverse.controller;

import com.comicverse.model.Comic;
import com.comicverse.repository.ComicRepository;
import com.comicverse.util.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SearchRestController {

    @Autowired
    private ComicRepository comicRepository;

    // ✅ SỬA Ở ĐÂY: Đổi <String, Object> thành <String, String>
    @GetMapping("/api/search-suggest")
    public List<Map<String, String>> searchSuggest(@RequestParam String keyword) {
        
        // 1. Tạo slug từ từ khóa (VD: "Hầm ngục" -> "ham-nguc")
        String slugKeyword = SlugUtils.toSlug(keyword);

        // 2. Tìm kiếm (Lấy tối đa 5 kết quả)
        // ⚠️ Lưu ý: Đảm bảo bạn đã thêm hàm searchComics vào ComicRepository như hướng dẫn trước
        List<Comic> comics = comicRepository.searchComics(keyword, slugKeyword);
        
        // 3. Trả về kết quả
        return comics.stream().limit(5).map(c -> Map.of(
            "title", c.getTitle(),
            "slug", c.getSlug(),
            "cover", c.getCoverImage(),
            "chapter", c.getLatestChapterNum(), 
            "status", c.getStatus().toString()
        )).collect(Collectors.toList());
    }
}