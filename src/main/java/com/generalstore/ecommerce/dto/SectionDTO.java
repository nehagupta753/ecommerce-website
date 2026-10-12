package com.generalstore.ecommerce.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SectionDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private Integer displayOrder;
    private Long categoryId;
    private String categoryName;
    private int productCount;
}
