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

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {
    private final ShoppingCartFacade shoppingCartFacade;

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {

        log.info("От пользователя {} получен GET-запрос на просмотр его корзины.", username);
        return shoppingCartFacade.getShoppingCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProducts(@RequestParam String username,
                                             @RequestBody @NotEmpty Map<UUID, @Min(1) Long> products) {
        log.info("От пользователя {} получен PUT-запрос на добавление товаров в корзину: {}.",
                username, products);
        return shoppingCartFacade.addProducts(username, products);
    }

    @DeleteMapping
    public void deactivateShoppingCart(@RequestParam String username) {
        log.info("От пользователя {} получен DELETE-запрос на удаление его корзины.", username);
        shoppingCartFacade.deactivateShoppingCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam String username,
                                                  @RequestBody @NotEmpty List<UUID> products) {
        log.info("От пользователя {} получен POST-запрос на удаление товаров из корзины: {}.",
                username, products);
        return shoppingCartFacade.removeProducts(username, products);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductsQuantity(@RequestParam String username,
                                                  @RequestBody @Valid ChangeProductQuantityRequest request) {
        log.info("От пользователя {} получен POST-запрос на изменение количества товара из корзине: {}.",
                username, request);
        return shoppingCartFacade.changeProductsQuantity(username, request);
    }
}
