package com.highspring.shopping.repository;

import com.highspring.shopping.entity.CategoryDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDiscountRepository extends JpaRepository<CategoryDiscount, Long> {
    List<CategoryDiscount> findByCategoryId(Long categoryId);
}
