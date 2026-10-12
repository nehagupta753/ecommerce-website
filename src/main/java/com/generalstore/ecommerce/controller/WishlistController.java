package com.generalstore.ecommerce.controller;

import com.generalstore.ecommerce.dto.WishlistItemDTO;
import com.generalstore.ecommerce.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<?> getWishlist(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Please login"));

        List<WishlistItemDTO> items = wishlistService.getWishlistItems(userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Long> body, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Please login"));

        try {
            WishlistItemDTO item = wishlistService.addToWishlist(userId, body.get("productId"));
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Please login"));

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<?> isInWishlist(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.ok(Map.of("inWishlist", false));

        return ResponseEntity.ok(Map.of("inWishlist", wishlistService.isInWishlist(userId, productId)));
    }
}
