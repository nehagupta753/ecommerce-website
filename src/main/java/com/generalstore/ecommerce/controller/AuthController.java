package com.generalstore.ecommerce.controller;

import com.generalstore.ecommerce.dto.AuthDTO;
import com.generalstore.ecommerce.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO.RegisterRequest request, HttpSession session) {
        AuthDTO.AuthResponse response = authService.register(request);
        if (response.getId() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        session.setAttribute("userId", response.getId());
        session.setAttribute("username", response.getUsername());
        session.setAttribute("role", response.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest request, HttpSession session) {
        AuthDTO.AuthResponse response = authService.login(request);
        if (response.getId() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        // Store user info in session
        session.setAttribute("userId", response.getId());
        session.setAttribute("username", response.getUsername());
        session.setAttribute("role", response.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }
        return ResponseEntity.ok(Map.of(
                "id", userId,
                "username", session.getAttribute("username"),
                "role", session.getAttribute("role")
        ));
    }
}
