package com.prison.service;

import com.prison.dto.ProductDTO;
import com.prison.entity.Product;
import com.prison.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(ProductDTO dto) {
        if (productRepository.findByProductCode(dto.getProductCode()).isPresent()) {
            throw new RuntimeException("商品编码已存在");
        }
        Product product = new Product();
        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setUnitPrice(dto.getUnitPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setMonthlyLimitPerPerson(dto.getMonthlyLimitPerPerson());
        product.setPurchaseRestrictionLevel(dto.getPurchaseRestrictionLevel());
        product.setIsOnSale(dto.getIsOnSale());
        product.setDescription(dto.getDescription());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        if (!product.getProductCode().equals(dto.getProductCode())) {
            if (productRepository.findByProductCode(dto.getProductCode()).isPresent()) {
                throw new RuntimeException("商品编码已存在");
            }
        }
        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setUnitPrice(dto.getUnitPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setMonthlyLimitPerPerson(dto.getMonthlyLimitPerPerson());
        product.setPurchaseRestrictionLevel(dto.getPurchaseRestrictionLevel());
        product.setDescription(dto.getDescription());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("商品不存在");
        }
        productRepository.deleteById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public Product getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public Page<Product> listProducts(String category, Boolean isOnSale, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return productRepository.search(keyword, pageable);
        }
        if (category != null && !category.isBlank() && isOnSale != null) {
            return productRepository.findByCategoryAndIsOnSale(category, isOnSale, pageable);
        }
        if (category != null && !category.isBlank()) {
            return productRepository.findByCategory(category, pageable);
        }
        if (isOnSale != null) {
            return productRepository.findByIsOnSale(isOnSale, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product setOnSale(Long id, boolean isOnSale) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setIsOnSale(isOnSale);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("库存不足");
        }
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    public List<Product> getAllOnSaleProducts() {
        return productRepository.findByIsOnSaleTrue();
    }

    public List<Object[]> getCategoryStats() {
        return productRepository.countByCategory();
    }
}
