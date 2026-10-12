package com.generalstore.ecommerce.dto;

import lombok.*;

public class AuthDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String phone;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String fullName;
        private String message;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CheckoutRequest {
        private String shippingAddress;
        private String phone;
        private String customerName;
        private String paymentMethod; // COD, CARD, UPI
    }
}
