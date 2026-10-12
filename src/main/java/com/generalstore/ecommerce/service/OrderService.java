package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.AuthDTO;
import com.generalstore.ecommerce.dto.OrderDTO;
import com.generalstore.ecommerce.dto.OrderItemDTO;
import com.generalstore.ecommerce.entity.*;
import com.generalstore.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO placeOrder(Long userId, AuthDTO.CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock and calculate total
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (!product.isInStock()) {
                throw new RuntimeException("Product '" + product.getName() + "' is out of stock");
            }
            if (product.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for '" + product.getName() + "'");
            }
            totalAmount += product.getPrice() * item.getQuantity();
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status("CONFIRMED")
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .customerName(request.getCustomerName())
                .build();

        order = orderRepository.save(order);

        // Create order items and deduct stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .productName(product.getName())
                    .productImageUrl(product.getImageUrl())
                    .build();

            order.getItems().add(orderItem);

            // Deduct stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUserId(userId);

        return toDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return toDTO(orderRepository.save(order));
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .productImageUrl(item.getProductImageUrl())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .phone(order.getPhone())
                .customerName(order.getCustomerName())
                .createdAt(order.getCreatedAt())
                .items(items)
                .username(order.getUser().getUsername())
                .build();
    }
}
