package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCart getShoppingCart(String username);

    ShoppingCart getActiveShoppingCart(String username);

    void deactivateShoppingCart(String username);

    ShoppingCart removeProducts(String username, List<UUID> products);

    ShoppingCart addProducts(String username, Map<UUID, Long> products);

    ShoppingCart changeProductsQuantity(String username, ChangeProductQuantityRequest request);

    void validateProductExists(UUID productId, ShoppingCart shoppingCart);
}
