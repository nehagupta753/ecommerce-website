package com.generalstore.ecommerce.repository;

import com.generalstore.ecommerce.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);
    boolean existsByNameAndCategoryId(String name, Long categoryId);
    java.util.Optional<Section> findByNameAndCategoryId(String name, Long categoryId);
}
