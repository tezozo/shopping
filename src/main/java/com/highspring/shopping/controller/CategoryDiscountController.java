package com.highspring.shopping.controller;

import com.highspring.shopping.entity.CategoryDiscount;
import com.highspring.shopping.repository.CategoryDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-discounts")
@RequiredArgsConstructor
public class CategoryDiscountController {

    private final CategoryDiscountRepository categoryDiscountRepository;

    @GetMapping
    public List<CategoryDiscount> getAll() {
        return categoryDiscountRepository.findAll();
    }
}
