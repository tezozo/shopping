package com.highspring.shopping.service;

import com.highspring.shopping.dto.UpdateShoppingCartRequest;
import com.highspring.shopping.entity.ShoppingCart;
import com.highspring.shopping.entity.ShoppingCartItem;
import com.highspring.shopping.repository.ItemRepository;
import com.highspring.shopping.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ShoppingCart create(String owner, String name) {
        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(owner);
        cart.setName(name != null ? name : "My Cart");
        cart.setCreatedOn(ZonedDateTime.now());
        return cartRepository.save(cart);
    }

    public List<ShoppingCart> getByOwner(String owner) {
        return cartRepository.findByOwnerAndDeletedFalse(owner);
    }

    public Optional<ShoppingCart> getById(UUID id) {
        return cartRepository.findById(id).filter(c -> !c.isDeleted());
    }

    @Transactional
    public boolean update(UUID id, UpdateShoppingCartRequest req) {
        return cartRepository.findById(id)
            .filter(c -> !c.isDeleted())
            .map(cart -> {
                if (req.name() != null) cart.setName(req.name());
                if (req.owner() != null) cart.setOwner(req.owner());
                if (req.items() != null) {
                    cart.getItems().clear();
                    req.items().forEach(r -> itemRepository.findById(r.itemId()).ifPresent(item -> {
                        ShoppingCartItem sci = new ShoppingCartItem();
                        sci.setItem(item);
                        sci.setUnits(r.units());
                        cart.getItems().add(sci);
                    }));
                }
                cartRepository.save(cart);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean delete(UUID id) {
        return cartRepository.findById(id)
            .filter(c -> !c.isDeleted())
            .map(cart -> {
                cart.setDeleted(true);
                cart.setDeletedOn(ZonedDateTime.now());
                cartRepository.save(cart);
                return true;
            })
            .orElse(false);
    }
}
