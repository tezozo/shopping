package com.highspring.shopping.controller;

import com.highspring.shopping.entity.Discount;
import com.highspring.shopping.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountRepository discountRepository;

    @GetMapping
    public List<Discount> getAll() {
        return discountRepository.findAll();
    }

    @PostMapping
    public Discount create(@RequestBody Discount discount) {
        return discountRepository.save(discount);
    }
}
