package ru.yandex.practicum.facade;

import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartFacade {
    ShoppingCartDto getShoppingCart(String username);

    void deactivateShoppingCart(String username);

    ShoppingCartDto removeProducts(String username, List<UUID> products);

    ShoppingCartDto addProducts(String username, Map<UUID, Long> products);

    ShoppingCartDto changeProductsQuantity(String username, ChangeProductQuantityRequest request);
}
