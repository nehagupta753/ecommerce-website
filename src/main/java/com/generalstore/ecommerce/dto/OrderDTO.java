package com.generalstore.ecommerce.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDTO {
    private Long id;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String phone;
    private String customerName;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
    // For admin view
    private String username;
}
