package com.generalstore.ecommerce.controller;

import com.generalstore.ecommerce.dto.ProductAdminDTO;
import com.generalstore.ecommerce.dto.ProductDTO;
import com.generalstore.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ========== Customer-facing endpoints (no quantity) ==========

    @GetMapping("/bestsellers")
    public ResponseEntity<List<ProductDTO>> getBestsellers(
            @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(productService.getBestsellers(category));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<ProductDTO>> getProductsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(productService.getProductsBySection(sectionId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    // ========== Admin-facing endpoints (includes quantity) ==========

    @GetMapping("/admin/all")
    public ResponseEntity<List<ProductAdminDTO>> getAllProductsAdmin() {
        return ResponseEntity.ok(productService.getAllProductsAdmin());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ProductAdminDTO> getProductAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductByIdAdmin(id));
    }

    @PostMapping
    public ResponseEntity<ProductAdminDTO> createProduct(
            @RequestParam("sectionId") Long sectionId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(productService.createProduct(sectionId, name, description, price, quantity, image));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductAdminDTO> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(productService.updateProduct(id, name, description, price, quantity, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    @GetMapping("/admin/low-stock")
    public ResponseEntity<List<ProductAdminDTO>> getLowStockProducts(
            @RequestParam(value = "threshold", defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }
}
