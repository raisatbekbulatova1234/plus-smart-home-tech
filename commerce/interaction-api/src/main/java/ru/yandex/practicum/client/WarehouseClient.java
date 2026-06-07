package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

/**
 * Feign клиент для взаимодействия с сервисом склада (warehouse)
 *
 * Используется другими микросервисами (корзина, заказы) для:
 * - Добавления новых товаров на склад
 * - Проверки наличия товаров в корзине
 * - Пополнения остатков товаров
 * - Получения адреса склада для расчета доставки
 *
 * name = "warehouse" - имя сервиса в Eureka (Service Discovery)
 * path = "/api/v1/warehouse" - базовый путь для всех запросов к сервису
 */
@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient {
    @PutMapping
    void addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest newProductRequest);

    @PostMapping("/check")
    BookedProductsDto checkShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCart);

    @PostMapping("/add")
    void addProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest addProductRequest);

    @GetMapping("/address")
    AddressDto getAddress();
}