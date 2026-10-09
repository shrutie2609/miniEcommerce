package com.example.shop.config;

import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    // Runs once at startup; only inserts if the table is empty
    @Bean
    CommandLineRunner seed(ProductRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                    new Product("Steel Water Bottle", "750 ml, keeps drinks cold for 24 hours", new BigDecimal("499"), 25, "\uD83E\uDDF4"),
                    new Product("Notebook Set", "Pack of 3 ruled notebooks, 120 pages each", new BigDecimal("249"), 40, "\uD83D\uDCD3"),
                    new Product("Desk Lamp", "Adjustable LED lamp with warm light", new BigDecimal("899"), 12, "\uD83D\uDCA1"),
                    new Product("Backpack", "20 L daily backpack with laptop sleeve", new BigDecimal("1299"), 15, "\uD83C\uDF92"),
                    new Product("Wireless Mouse", "Quiet-click mouse, 12 month battery", new BigDecimal("699"), 30, "\uD83D\uDDB1\uFE0F"),
                    new Product("Coffee Mug", "Ceramic mug, 350 ml, dishwasher safe", new BigDecimal("199"), 50, "\u2615")
            ));
        };
    }
}
