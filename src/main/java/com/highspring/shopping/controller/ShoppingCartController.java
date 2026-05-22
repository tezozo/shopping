package com.highspring.shopping.controller;

import com.highspring.shopping.dto.CreateShoppingCartRequest;
import com.highspring.shopping.dto.UpdateShoppingCartRequest;
import com.highspring.shopping.entity.ShoppingCart;
import com.highspring.shopping.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shopping-carts")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService service;

    @PostMapping
    public ResponseEntity<ShoppingCart> create(@RequestBody CreateShoppingCartRequest req) {
        return ResponseEntity.ok(service.create(req.owner(), req.name()));
    }

    @GetMapping
    public ResponseEntity<List<ShoppingCart>> getAll(@RequestParam String owner) {
        return ResponseEntity.ok(service.getByOwner(owner));
    }

    @GetMapping("/owners")
    public ResponseEntity<List<String>> getOwners() {
        return ResponseEntity.ok(service.getDistinctOwners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingCart> getById(@PathVariable UUID id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> update(@PathVariable UUID id,
                                                       @RequestBody UpdateShoppingCartRequest req) {
        return ResponseEntity.ok(Map.of("success", service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("success", service.delete(id)));
    }
}
