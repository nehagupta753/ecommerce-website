package com.generalstore.ecommerce.service;

import com.generalstore.ecommerce.dto.ProductAdminDTO;
import com.generalstore.ecommerce.dto.ProductDTO;
import com.generalstore.ecommerce.entity.Product;
import com.generalstore.ecommerce.entity.Section;
import com.generalstore.ecommerce.repository.OrderItemRepository;
import com.generalstore.ecommerce.repository.ProductRepository;
import com.generalstore.ecommerce.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SectionRepository sectionRepository;
    private final OrderItemRepository orderItemRepository;
    private final FileStorageService fileStorageService;

    // Customer-facing: Bestsellers ranked strictly by highest sales count, filtered for products with sales above average
    public List<ProductDTO> getBestsellers(String categoryName) {
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) {
            return List.of();
        }

        // Fetch sales counts grouped by product
        List<Object[]> salesData = orderItemRepository.findSalesCountGroupedByProduct();
        Map<Long, Long> salesMap = new HashMap<>();
        for (Object[] row : salesData) {
            Long pid = (Long) row[0];
            Number count = (Number) row[1];
            salesMap.put(pid, count != null ? count.longValue() : 0L);
        }

        // Filter by category if requested
        List<Product> filtered = allProducts;
        if (categoryName != null && !categoryName.isBlank() && !categoryName.equalsIgnoreCase("all")) {
            filtered = allProducts.stream()
                    .filter(p -> p.getSection() != null 
                              && p.getSection().getCategory() != null 
                              && p.getSection().getCategory().getName().equalsIgnoreCase(categoryName))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            return List.of();
        }

        // Calculate average sales
        double totalSales = filtered.stream()
                .mapToLong(p -> salesMap.getOrDefault(p.getId(), 0L))
                .sum();
        double avgSales = totalSales / filtered.size();

        // Products with sales above average, ordered descending by sales count
        List<ProductDTO> bestsellers = filtered.stream()
                .filter(p -> salesMap.getOrDefault(p.getId(), 0L) > avgSales)
                .sorted((a, b) -> Long.compare(salesMap.getOrDefault(b.getId(), 0L), salesMap.getOrDefault(a.getId(), 0L)))
                .map(p -> toCustomerDTO(p, salesMap.getOrDefault(p.getId(), 0L)))
                .collect(Collectors.toList());

        // If no products have sales yet (e.g. initial launch), rank by sales descending (or top featured items)
        if (bestsellers.isEmpty()) {
            bestsellers = filtered.stream()
                    .sorted((a, b) -> Long.compare(salesMap.getOrDefault(b.getId(), 0L), salesMap.getOrDefault(a.getId(), 0L)))
                    .limit(6)
                    .map(p -> toCustomerDTO(p, salesMap.getOrDefault(p.getId(), 0L)))
                    .collect(Collectors.toList());
        }

        return bestsellers;
    }

    // Customer-facing: no quantity exposed
    public List<ProductDTO> getProductsBySection(Long sectionId) {
        return productRepository.findBySectionIdOrderByCreatedAtDesc(sectionId)
                .stream()
                .map(this::toCustomerDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findBySectionCategoryIdOrderByCreatedAtDesc(categoryId)
                .stream()
                .map(this::toCustomerDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toCustomerDTO(product);
    }

    public List<ProductDTO> searchProducts(String query) {
        return productRepository.searchProducts(query)
                .stream()
                .map(this::toCustomerDTO)
                .collect(Collectors.toList());
    }

    // Admin-facing: includes quantity
    public List<ProductAdminDTO> getAllProductsAdmin() {
        return productRepository.findAll()
                .stream()
                .map(this::toAdminDTO)
                .collect(Collectors.toList());
    }

    public ProductAdminDTO getProductByIdAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toAdminDTO(product);
    }

    public ProductAdminDTO createProduct(Long sectionId, String name, String description, Double price, Integer quantity, MultipartFile image) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .quantity(quantity != null ? quantity : 0)
                .section(section)
                .build();

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(fileStorageService.storeFile(image));
        }

        return toAdminDTO(productRepository.save(product));
    }

    public ProductAdminDTO updateProduct(Long id, String name, String description, Double price, Integer quantity, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(price);
        if (quantity != null) product.setQuantity(quantity);
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(product.getImageUrl());
            product.setImageUrl(fileStorageService.storeFile(image));
        }

        return toAdminDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        fileStorageService.deleteFile(product.getImageUrl());
        productRepository.delete(product);
    }

    public List<ProductAdminDTO> getLowStockProducts(int threshold) {
        return productRepository.findByQuantityLessThanEqual(threshold)
                .stream()
                .map(this::toAdminDTO)
                .collect(Collectors.toList());
    }

    // Customer DTO — quantity is NEVER included
    private ProductDTO toCustomerDTO(Product product) {
        return toCustomerDTO(product, 0L);
    }

    private ProductDTO toCustomerDTO(Product product, Long salesCount) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .inStock(product.isInStock())
                .sectionId(product.getSection().getId())
                .sectionName(product.getSection().getName())
                .categoryName(product.getSection().getCategory().getName())
                .salesCount(salesCount)
                .build();
    }

    // Admin DTO — includes quantity
    private ProductAdminDTO toAdminDTO(Product product) {
        return ProductAdminDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .quantity(product.getQuantity())
                .inStock(product.isInStock())
                .sectionId(product.getSection().getId())
                .sectionName(product.getSection().getName())
                .categoryName(product.getSection().getCategory().getName())
                .categoryId(product.getSection().getCategory().getId())
                .build();
    }
}
