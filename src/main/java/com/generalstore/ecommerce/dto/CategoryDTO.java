package com.generalstore.ecommerce.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private Integer displayOrder;
    private List<SectionDTO> sections;
}
