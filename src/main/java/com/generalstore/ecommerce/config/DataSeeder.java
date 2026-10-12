package com.generalstore.ecommerce.config;

import com.generalstore.ecommerce.entity.Category;
import com.generalstore.ecommerce.entity.Product;
import com.generalstore.ecommerce.entity.Section;
import com.generalstore.ecommerce.entity.User;
import com.generalstore.ecommerce.repository.CategoryRepository;
import com.generalstore.ecommerce.repository.ProductRepository;
import com.generalstore.ecommerce.repository.SectionRepository;
import com.generalstore.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedCategoriesAndProducts();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User owner = User.builder()
                    .username("admin")
                    .email("admin@generalstore.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("OWNER")
                    .fullName("Store Owner")
                    .build();
            userRepository.save(owner);
            System.out.println("✅ Default owner account created: admin / admin123");
        }
    }

    private void seedCategoriesAndProducts() {
        // --- Hair Care Category ---
        Category hairCare = getOrCreateCategory("Hair Care", "Nourishing and restorative hair care essentials");
        Section shampoo = getOrCreateSection("Shampoo", hairCare, "Gentle and deep cleansing shampoos");
        Section conditioner = getOrCreateSection("Conditioner", hairCare, "Smoothing and hydrating hair conditioners");

        // Shampoo products
        seedProduct(
                "L'Oreal Paris Hyaluron Moisture 72H Hydrating Shampoo",
                "Enriched with Hyaluronic Acid for dehydrated hair. Fills hair fiber with 72 hours of moisture for bouncy, shiny, and weightless hair.",
                449.0, 25, shampoo, null
        );
        seedProduct(
                "Love Beauty and Planet Onion & Black Seed Oil Shampoo",
                "Natural, vegan shampoo made with pure steam-distilled onion bulb oil and black seed oil for visibly strong and nourished strands.",
                499.0, 20, shampoo, null
        );
        seedProduct(
                "Plum Coconut Milk & Peptides Strength & Shine Shampoo",
                "Gentle, sulfate-free nourishing shampoo infused with Coconut Milk and plant-based Peptides to repair damaged, dull hair.",
                380.0, 30, shampoo, null
        );
        seedProduct(
                "WishCare Multi-Peptide Anti-Hairfall Shampoo",
                "Fortified with Multi-Peptides, Biotin, Caffeine, and Fermented Rice Water to stimulate scalp health and reduce hair thinning.",
                549.0, 20, shampoo, null
        );

        // Conditioner products
        seedProduct(
                "TRESemme Keratin Smooth Conditioner",
                "Formulated with Keratin & Argan Oil to deliver up to 72 hours of frizz control, silky shine, and salon-soft smoothness.",
                349.0, 35, conditioner, null
        );
        seedProduct(
                "Love Beauty and Planet Murumuru Butter & Rose Blooming Colour Conditioner",
                "Deeply moisturizing conditioner infused with Amazonian Murumuru Butter and fresh Bulgarian Rose petals for glossy, vibrant hair.",
                475.0, 25, conditioner, null
        );

        // --- Skincare Category ---
        Category skincare = getOrCreateCategory("Skincare", "Targeted skincare formulas for glowing, healthy skin");
        Section serum = getOrCreateSection("Serum", skincare, "Active serums for skin radiance and repair");
        Section facewash = getOrCreateSection("Facewash", skincare, "Clarifying and barrier-protecting facial cleansers");
        Section moisturizer = getOrCreateSection("Moisturizer", skincare, "Deep nourishing and barrier repair moisturizers");

        // Serum products
        seedProduct(
                "Minimalist 10% Vitamin C Face Serum",
                "Formulated with stable Ethyl Ascorbic Acid and Centella Asiatica to brighten dull skin, even skin tone, and reduce dark spots.",
                699.0, 40, serum, null
        );
        seedProduct(
                "Plum 10% Niacinamide Face Serum with Rice Water",
                "Powered with 10% Niacinamide and Japanese Fermented Rice Water to clear blemishes, shrink enlarged pores, and improve skin barrier.",
                599.0, 35, serum, null
        );

        // Facewash products
        seedProduct(
                "Himalaya Purifying Neem Face Wash",
                "Clinically proven soap-free herbal formulation that removes excess oil and impurities, preventing acne and pimples naturally.",
                180.0, 50, facewash, null
        );
        seedProduct(
                "Dot & Key Barrier Repair Gentle Hydrating Face Wash",
                "Sulfate-free hydrating facial cleanser with 5 Essential Ceramides and Hyaluronic Acid to gently cleanse while preserving skin barrier moisture.",
                295.0, 30, facewash, null
        );
        seedProduct(
                "The Derma Co 2% Sali-Cinamide Anti-Acne Face Wash",
                "Dermatologist-designed cleanser containing 2% Salicylic Acid and 2% Niacinamide to combat active acne, soothe inflammation, and unclog pores.",
                299.0, 30, facewash, null
        );

        // Moisturizer products
        seedProduct(
                "Re'equil Ceramide & Hyaluronic Acid Moisturiser",
                "Deep hydrating cream enriched with Ceramides and Hyaluronic Acid to reinforce the skin's lipid barrier and relieve extreme dryness.",
                395.0, 35, moisturizer, null
        );

        // --- Jewellery Category ---
        Category jewellery = getOrCreateCategory("Jewellery", "Handcrafted fine jewellery, charms, and timeless adornments");
        Section bracelet = getOrCreateSection("Bracelet", jewellery, "Delicate charm, gemstone, and filigree bracelets");

        seedProduct(
                "Rose Quartz & Freshwater Pearl Seashell Charm Bracelet",
                "Handcrafted with pastel pink rose quartz beads, luminous freshwater pearls, an enamel butterfly charm, and an intricate golden seashell pendant with central pearl.",
                170.0, 25, bracelet, "/uploads/50f45d8b-4913-4e82-a379-34d140e17d49.png"
        );
        seedProduct(
                "Vintage Filigree Silver Leaf Link Bracelet",
                "Artisanal oxidized silver filigree link bracelet featuring sculpted teardrop leaf filigree motifs connected with an adjustable S-hook clasp.",
                200.0, 20, bracelet, "/uploads/9f4515e3-f7b3-43cd-87ed-8c6d8cc222a2.png"
        );
        seedProduct(
                "Dainty Gold Heart & Evil Eye Charm Bracelet",
                "Minimalist 18K gold-plated delicate link chain adorned with dangling polished golden hearts and a protective blue glass evil eye talisman.",
                150.0, 30, bracelet, "/uploads/8726384a-0f3d-4cfe-8f94-fb31224ff2e4.png"
        );
        seedProduct(
                "Trio Satellite Stacking Bead Chain Bracelet",
                "Ultra-delicate trio of satellite bead chain bracelets crafted in rose gold, yellow gold, and silver tones for an effortless layered wrist stack.",
                120.0, 35, bracelet, "/uploads/999d174f-7a99-4445-8804-046b01542a8b.png"
        );
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .displayOrder(0)
                        .build()));
    }

    private Section getOrCreateSection(String name, Category category, String description) {
        return sectionRepository.findByNameAndCategoryId(name, category.getId())
                .orElseGet(() -> sectionRepository.save(Section.builder()
                        .name(name)
                        .category(category)
                        .description(description)
                        .displayOrder(0)
                        .build()));
    }

    private void seedProduct(String name, String description, Double price, Integer quantity, Section section, String imageUrl) {
        if (!productRepository.existsByNameAndSectionId(name, section.getId())) {
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .quantity(quantity != null ? quantity : 10)
                    .section(section)
                    .imageUrl(imageUrl)
                    .build();
            productRepository.save(product);
            System.out.println("✅ Seeded product: " + name + " in section: " + section.getName());
        }
    }
}
