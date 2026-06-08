package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {
    private final WarehouseService warehouseService;

    @PutMapping
    public void addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest newProductRequest) {
        log.info("Получен PUT-запрос на добавление нового товара с id {} на склад.", newProductRequest.getProductId());
        warehouseService.addNewProduct(newProductRequest);
    }

    @PostMapping("/check")
    public BookedProductsDto checkShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCart) {
        log.info("Получен POST-запрос на проверку комплектности корзины заказов с id {}.", shoppingCart.getShoppingCartId());
        return warehouseService.checkShoppingCart(shoppingCart);
    }

    @PostMapping("/add")
    public void addProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest addProductRequest) {
        log.info("Получен POST-запрос на прием товара с id {} на складе.", addProductRequest.getProductId());
        warehouseService.addProductQuantity(addProductRequest);
    }

    @GetMapping("/address")
    public AddressDto getAddress() {
        log.info("Получен GET-запрос на получение адреса склада.");
        return warehouseService.getAddress();
    }
}
