package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.WishlistItemDTO;
import com.generalstore.ecommerce.entity.Product;
import com.generalstore.ecommerce.entity.User;
import com.generalstore.ecommerce.entity.WishlistItem;
import com.generalstore.ecommerce.repository.ProductRepository;
import com.generalstore.ecommerce.repository.UserRepository;
import com.generalstore.ecommerce.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<WishlistItemDTO> getWishlistItems(Long userId) {
        return wishlistItemRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public WishlistItemDTO addToWishlist(Long userId, Long productId) {
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        return toDTO(wishlistItemRepository.save(item));
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    private WishlistItemDTO toDTO(WishlistItem item) {
        Product p = item.getProduct();
        return WishlistItemDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImageUrl(p.getImageUrl())
                .productPrice(p.getPrice())
                .productInStock(p.isInStock())
                .build();
    }
}
