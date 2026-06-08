package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.ShoppingCartClient;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.facade.ShoppingCartFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST контроллер для управления корзиной покупок
 * Реализует интерфейс ShoppingCartClient для использования через Feign
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartFacade shoppingCartFacade;

    /**
     * GET /api/v1/shopping-cart?username=user@example.com
     * Получение корзины пользователя
     */
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        log.info("От пользователя {} получен GET-запрос на просмотр его корзины.", username);
        return shoppingCartFacade.getShoppingCart(username);
    }

    /**
     * PUT /api/v1/shopping-cart?username=user@example.com
     * Добавление товаров в корзину
     */
    @PutMapping
    public ShoppingCartDto addProducts(
            @RequestParam String username,
            @RequestBody @NotEmpty Map<UUID, @Min(1) Long> products) {  // Валидация: не пустой, мин. количество 1
        log.info("От пользователя {} получен PUT-запрос на добавление товаров в корзину: {}.",
                username, products);
        return shoppingCartFacade.addProducts(username, products);
    }

    /**
     * DELETE /api/v1/shopping-cart?username=user@example.com
     * Деактивация корзины (мягкое удаление, меняет статус на DEACTIVATE)
     */
    @DeleteMapping
    public void deactivateShoppingCart(@RequestParam String username) {
        log.info("От пользователя {} получен DELETE-запрос на удаление его корзины.", username);
        shoppingCartFacade.deactivateShoppingCart(username);
    }

    /**
     * POST /api/v1/shopping-cart/remove?username=user@example.com
     * Удаление конкретных товаров из корзины
     */
    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(
            @RequestParam String username,
            @RequestBody @NotEmpty List<UUID> products) {
        log.info("От пользователя {} получен POST-запрос на удаление товаров из корзины: {}.",
                username, products);
        return shoppingCartFacade.removeProducts(username, products);
    }

    /**
     * POST /api/v1/shopping-cart/change-quantity?username=user@example.com
     * Изменение количества конкретного товара в корзине
     */
    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductsQuantity(
            @RequestParam String username,
            @RequestBody @Valid ChangeProductQuantityRequest request) {
        log.info("От пользователя {} получен POST-запрос на изменение количества товара в корзине: {}.",
                username, request);
        return shoppingCartFacade.changeProductsQuantity(username, request);
    }
}