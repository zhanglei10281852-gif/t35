package com.prison.repository;

import com.prison.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByIsOnSale(Boolean isOnSale, Pageable pageable);

    Page<Product> findByCategoryAndIsOnSale(String category, Boolean isOnSale, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.productCode LIKE %:keyword%")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findByIsOnSaleTrue();

    @Query("SELECT p.category, SUM(p.stockQuantity) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();
}
