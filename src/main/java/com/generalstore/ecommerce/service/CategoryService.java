package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.CategoryDTO;
import com.generalstore.ecommerce.dto.SectionDTO;
import com.generalstore.ecommerce.entity.Category;
import com.generalstore.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return toDTO(category);
    }

    public CategoryDTO createCategory(String name, String description, Integer displayOrder, MultipartFile image) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();

        if (image != null && !image.isEmpty()) {
            category.setImageUrl(fileStorageService.storeFile(image));
        }

        return toDTO(categoryRepository.save(category));
    }

    public CategoryDTO updateCategory(Long id, String name, String description, Integer displayOrder, MultipartFile image) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (displayOrder != null) category.setDisplayOrder(displayOrder);
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(category.getImageUrl());
            category.setImageUrl(fileStorageService.storeFile(image));
        }

        return toDTO(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        fileStorageService.deleteFile(category.getImageUrl());
        categoryRepository.delete(category);
    }

    private CategoryDTO toDTO(Category category) {
        List<SectionDTO> sectionDTOs = category.getSections() != null
                ? category.getSections().stream()
                    .map(s -> SectionDTO.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .imageUrl(s.getImageUrl())
                            .description(s.getDescription())
                            .displayOrder(s.getDisplayOrder())
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .productCount(s.getProducts() != null ? s.getProducts().size() : 0)
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .sections(sectionDTOs)
                .build();
    }
}
