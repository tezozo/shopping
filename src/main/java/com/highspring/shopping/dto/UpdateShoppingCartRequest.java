package com.highspring.shopping.dto;

import java.util.List;

public record UpdateShoppingCartRequest(String name, String owner, List<ShoppingCartItemRequest> items) {
}
