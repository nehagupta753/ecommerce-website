package com.generalstore.ecommerce.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductAdminDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Integer quantity; // ONLY visible to owner
    private boolean inStock;
    private Long sectionId;
    private String sectionName;
    private String categoryName;
    private Long categoryId;
}
