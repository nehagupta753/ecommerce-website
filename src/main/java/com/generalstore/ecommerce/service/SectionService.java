package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.SectionDTO;
import com.generalstore.ecommerce.entity.Category;
import com.generalstore.ecommerce.entity.Section;
import com.generalstore.ecommerce.repository.CategoryRepository;
import com.generalstore.ecommerce.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public List<SectionDTO> getSectionsByCategory(Long categoryId) {
        return sectionRepository.findByCategoryIdOrderByDisplayOrderAsc(categoryId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SectionDTO getSectionById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        return toDTO(section);
    }

    public SectionDTO createSection(Long categoryId, String name, String description, Integer displayOrder, MultipartFile image) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Section section = Section.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .category(category)
                .build();

        if (image != null && !image.isEmpty()) {
            section.setImageUrl(fileStorageService.storeFile(image));
        }

        return toDTO(sectionRepository.save(section));
    }

    public SectionDTO updateSection(Long id, String name, String description, Integer displayOrder, MultipartFile image) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        if (name != null) section.setName(name);
        if (description != null) section.setDescription(description);
        if (displayOrder != null) section.setDisplayOrder(displayOrder);
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(section.getImageUrl());
            section.setImageUrl(fileStorageService.storeFile(image));
        }

        return toDTO(sectionRepository.save(section));
    }

    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        fileStorageService.deleteFile(section.getImageUrl());
        sectionRepository.delete(section);
    }

    private SectionDTO toDTO(Section section) {
        return SectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .imageUrl(section.getImageUrl())
                .description(section.getDescription())
                .displayOrder(section.getDisplayOrder())
                .categoryId(section.getCategory().getId())
                .categoryName(section.getCategory().getName())
                .productCount(section.getProducts() != null ? section.getProducts().size() : 0)
                .build();
    }
}
