package com.highspring.shopping.controller;

import com.highspring.shopping.entity.Tax;
import com.highspring.shopping.repository.TaxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taxes")
@RequiredArgsConstructor
public class TaxController {

    private final TaxRepository taxRepository;

    @GetMapping
    public List<Tax> getAll() {
        return taxRepository.findAll();
    }

    @PostMapping
    public Tax create(@RequestBody Tax tax) {
        return taxRepository.save(tax);
    }
}
