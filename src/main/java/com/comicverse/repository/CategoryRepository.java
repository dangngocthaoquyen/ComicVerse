package com.comicverse.repository;

import com.comicverse.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
    List<Category> findByStatusOrderByPriorityDesc(Category.Status status);
}