package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.AuthDTO;
import com.generalstore.ecommerce.entity.User;
import com.generalstore.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthDTO.AuthResponse.builder().message("Username already exists").build();
        }

        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = request.getUsername().toLowerCase().replaceAll("[^a-z0-9]", "") + "@customer.buzzygirly.com";
        }

        if (userRepository.existsByEmail(email)) {
            email = request.getUsername().toLowerCase().replaceAll("[^a-z0-9]", "") + "_" + System.currentTimeMillis() + "@customer.buzzygirly.com";
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("CUSTOMER")
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        user = userRepository.save(user);

        return AuthDTO.AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .message("Registration successful")
                .build();
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthDTO.AuthResponse.builder().message("Invalid username or password").build();
        }

        return AuthDTO.AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .message("Login successful")
                .build();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
