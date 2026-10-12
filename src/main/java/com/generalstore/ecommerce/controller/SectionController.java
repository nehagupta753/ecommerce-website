package com.generalstore.ecommerce.controller;

import com.generalstore.ecommerce.dto.SectionDTO;
import com.generalstore.ecommerce.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(sectionService.getSectionsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionDTO> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PostMapping
    public ResponseEntity<SectionDTO> createSection(
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(sectionService.createSection(categoryId, name, description, displayOrder, image));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionDTO> updateSection(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(sectionService.updateSection(id, name, description, displayOrder, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(Map.of("message", "Section deleted successfully"));
    }
}
