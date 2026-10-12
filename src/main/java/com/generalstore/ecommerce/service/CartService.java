package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.CartItemDTO;
import com.generalstore.ecommerce.entity.CartItem;
import com.generalstore.ecommerce.entity.Product;
import com.generalstore.ecommerce.entity.User;
import com.generalstore.ecommerce.repository.CartItemRepository;
import com.generalstore.ecommerce.repository.ProductRepository;
import com.generalstore.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<CartItemDTO> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CartItemDTO addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int available = product.getQuantity() != null ? product.getQuantity() : 0;
        if (available <= 0) {
            throw new RuntimeException("Product is out of stock");
        }

        int addQty = (quantity != null && quantity > 0) ? quantity : 1;

        // Check if item already in cart
        CartItem existing = cartItemRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        if (existing != null) {
            int newTotal = existing.getQuantity() + addQty;
            if (newTotal > available) {
                existing.setQuantity(available);
                cartItemRepository.save(existing);
                throw new RuntimeException("Product is out of stock! Only " + available + " item(s) available.");
            }
            existing.setQuantity(newTotal);
            return toDTO(cartItemRepository.save(existing));
        }

        if (addQty > available) {
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(available)
                    .build();
            cartItemRepository.save(cartItem);
            throw new RuntimeException("Product is out of stock! Only " + available + " item(s) available.");
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(addQty)
                .build();

        return toDTO(cartItemRepository.save(cartItem));
    }

    public CartItemDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int available = product.getQuantity() != null ? product.getQuantity() : 0;
        if (available <= 0) {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
            throw new RuntimeException("Product is out of stock");
        }

        if (quantity > available) {
            cartItem.setQuantity(available);
            cartItemRepository.save(cartItem);
            throw new RuntimeException("Product is out of stock! Only " + available + " item(s) available.");
        }

        cartItem.setQuantity(quantity);
        return toDTO(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public Double getCartTotal(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    private CartItemDTO toDTO(CartItem item) {
        Product p = item.getProduct();
        return CartItemDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImageUrl(p.getImageUrl())
                .productPrice(p.getPrice())
                .productInStock(p.isInStock())
                .quantity(item.getQuantity())
                .subtotal(p.getPrice() * item.getQuantity())
                .build();
    }
}
