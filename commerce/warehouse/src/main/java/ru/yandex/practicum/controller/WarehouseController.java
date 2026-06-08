package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

/**
 * REST контроллер для управления складом
 * Реализует интерфейс WarehouseClient для использования через Feign
 *
 * Эндпоинты:
 * - PUT /api/v1/warehouse - добавление нового товара
 * - POST /api/v1/warehouse/check - проверка корзины
 * - POST /api/v1/warehouse/add - пополнение остатков
 * - GET /api/v1/warehouse/address - получение адреса склада
 * - POST /api/v1/warehouse/assembly - сборка заказа (бронирование)
 * - POST /api/v1/warehouse/shipped - отгрузка товаров в доставку
 * - POST /api/v1/warehouse/return - возврат товаров на склад
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    // ==================== УПРАВЛЕНИЕ ТОВАРАМИ ====================

    /**
     * PUT /api/v1/warehouse
     * Добавление нового товара на склад
     */
    @PutMapping
    public void addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest newProductRequest) {
        log.info("Получен PUT-запрос на добавление нового товара с id {} на склад.",
                newProductRequest.getProductId());
        warehouseService.addNewProduct(newProductRequest);
    }

    /**
     * POST /api/v1/warehouse/check
     * Проверка корзины: хватает ли товаров на складе (без резервирования)
     */
    @PostMapping("/check")
    public BookedProductsDto checkShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCart) {
        log.info("Получен POST-запрос на проверку комплектности корзины заказов с id {}.",
                shoppingCart.getShoppingCartId());
        return warehouseService.checkShoppingCart(shoppingCart);
    }

    /**
     * POST /api/v1/warehouse/add
     * Пополнение остатков товара на складе
     */
    @PostMapping("/add")
    public void addProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest addProductRequest) {
        log.info("Получен POST-запрос на прием товара с id {} на складе.",
                addProductRequest.getProductId());
        warehouseService.addProductQuantity(addProductRequest);
    }

    /**
     * POST /api/v1/warehouse/assembly
     * Сборка заказа (бронирование товаров на складе)
     * Резервирует товары и возвращает параметры доставки
     */
    @PostMapping("/assembly")
    public BookedProductsDto assembleOrder(@RequestBody @Valid AssemblyProductsForOrderRequest assemblyRequest) {
        log.info("Получен POST-запрос на сбор заказа с id {}.", assemblyRequest.getOrderId());
        return warehouseService.assembleOrder(assemblyRequest);
    }

    /**
     * POST /api/v1/warehouse/shipped
     * Подтверждение отгрузки товаров в доставку
     * Обновляет бронь: добавляет ID доставки
     */
    @PostMapping("/shipped")
    public void shipProductsToDelivery(@RequestBody @Valid ShippedToDeliveryRequest shippedRequest) {
        log.info("Получен POST-запрос на отгрузку товара по заказу с id {}.",
                shippedRequest.getOrderId());
        warehouseService.shipProductsToDelivery(shippedRequest);
    }

    /**
     * POST /api/v1/warehouse/return
     * Возврат товаров на склад (при отмене заказа или возврате)
     */
    @PostMapping("/return")
    public void returnProducts(@RequestBody Map<@NotNull UUID, @NotNull @Min(1) Long> returnRequest) {
        log.info("Получен POST-запрос на возврат {} товаров на склад.", returnRequest.size());
        warehouseService.returnProducts(returnRequest);
    }

    // ==================== АДРЕС СКЛАДА ====================

    /**
     * GET /api/v1/warehouse/address
     * Получение адреса склада для расчета доставки
     */
    @GetMapping("/address")
    public AddressDto getAddress() {
        log.info("Получен GET-запрос на получение адреса склада.");
        return warehouseService.getAddress();
    }
}