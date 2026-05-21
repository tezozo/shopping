package com.highspring.shopping.repository;

import com.highspring.shopping.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    List<ShoppingCart> findByOwnerAndDeletedFalse(String owner);
}
