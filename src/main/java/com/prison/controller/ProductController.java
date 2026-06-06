package com.prison.controller;

import com.prison.dto.ProductDTO;
import com.prison.entity.Product;
import com.prison.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductDTO dto) {
        try {
            Product product = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        try {
            Product product = productService.updateProduct(id, dto);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<?> getByCode(@PathVariable String productCode) {
        try {
            return ResponseEntity.ok(productService.getProductByCode(productCode));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isOnSale,
            @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> result = productService.listProducts(category, isOnSale, keyword, pageRequest);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/onsale")
    public ResponseEntity<?> setOnSale(@PathVariable Long id, @RequestParam boolean isOnSale) {
        try {
            Product product = productService.setOnSale(id, isOnSale);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            Product product = productService.updateStock(id, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/onsale/all")
    public ResponseEntity<?> getAllOnSale() {
        List<Product> products = productService.getAllOnSaleProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats/category")
    public ResponseEntity<?> getCategoryStats() {
        List<Object[]> stats = productService.getCategoryStats();
        return ResponseEntity.ok(stats);
    }
}
