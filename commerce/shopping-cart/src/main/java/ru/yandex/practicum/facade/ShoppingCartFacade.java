package ru.yandex.practicum.facade;

import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Фасад для управления корзиной покупок
 *
 * Фасад объединяет бизнес-логику:
 * - Валидацию пользователя
 * - Взаимодействие с БД (через сервис корзины)
 * - Взаимодействие со складом (через клиент)
 * - Взаимодействие с магазином (через клиент)
 */
public interface ShoppingCartFacade {

    /**
     * Получение корзины пользователя
     * Если корзина не существует - создается новая
     */
    ShoppingCartDto getShoppingCart(String username);

    /**
     * Деактивация корзины пользователя (мягкое удаление)
     * Корзина помечается как DEACTIVATE, но не удаляется из БД
     */
    void deactivateShoppingCart(String username);

    /**
     * Удаление конкретных товаров из корзины
     */
    ShoppingCartDto removeProducts(String username, List<UUID> products);

    /**
     * Добавление товаров в корзину
     */
    ShoppingCartDto addProducts(String username, Map<UUID, Long> products);

    /**
     * Изменение количества конкретного товара в корзине
     */
    ShoppingCartDto changeProductsQuantity(String username, ChangeProductQuantityRequest request);
}