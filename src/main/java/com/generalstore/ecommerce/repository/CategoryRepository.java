package com.generalstore.ecommerce.repository;

import com.generalstore.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByDisplayOrderAsc();
    boolean existsByName(String name);
    java.util.Optional<Category> findByName(String name);
}
