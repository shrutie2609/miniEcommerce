package com.example.shop.product;

import com.example.shop.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log =
            LoggerFactory.getLogger(ProductController.class);

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {

        this.repository = repository;
    }

    @GetMapping
    public List<Product> getAll() {

        log.info("Fetching all products");

        List<Product> products = repository.findAll();

        log.info("Fetched {} products", products.size());

        return products;
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {

        log.info("Product requested with id {}", id);

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product {} not found", id);

                    return new ResourceNotFoundException(
                            "Product " + id + " not found"
                    );
                });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {

        log.info("Creating product with name {}", product.getName());

        product.setId(null);

        Product savedProduct = repository.save(product);

        log.info(
                "Product created successfully with id {}",
                savedProduct.getId()
        );

        return savedProduct;
    }
    @GetMapping("/test-error")
    public String testError() {

        try {
            throw new RuntimeException("Test error for ELK monitoring");

        } catch (Exception e) {
            log.error("Test ERROR generated for ELK monitoring", e);
            throw e;
        }
    }
}

