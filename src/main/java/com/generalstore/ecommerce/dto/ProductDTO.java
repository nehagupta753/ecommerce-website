package com.generalstore.ecommerce.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private boolean inStock; // derived from quantity > 0, quantity is NEVER exposed
    private Long sectionId;
    private String sectionName;
    private String categoryName;
    private Long salesCount;
}
