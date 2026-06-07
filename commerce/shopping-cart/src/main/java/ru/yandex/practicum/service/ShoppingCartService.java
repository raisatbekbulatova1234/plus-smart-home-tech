package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для работы с корзиной покупок
 * Содержит бизнес-логику управления корзиной
 */
public interface ShoppingCartService {
    //Получение корзины пользователя
    //Если корзина не существует - создается новая
    ShoppingCart getShoppingCart(String username);

    //Получение активной корзины пользователя
    ShoppingCart getActiveShoppingCart(String username);

    void deactivateShoppingCart(String username);

    ShoppingCart removeProducts(String username, List<UUID> products);

    ShoppingCart addProducts(String username, Map<UUID, Long> products);

    ShoppingCart changeProductsQuantity(String username, ChangeProductQuantityRequest request);

    void validateProductExists(UUID productId, ShoppingCart shoppingCart);
}