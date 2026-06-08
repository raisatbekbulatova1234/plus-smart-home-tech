package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

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

    @PostMapping("/assembly")
    BookedProductsDto assembleOrder(@RequestBody @Valid AssemblyProductsForOrderRequest assemblyRequest);

    @PostMapping("/shipped")
    void shipProductsToDelivery(@RequestBody @Valid ShippedToDeliveryRequest shippedRequest);

    @PostMapping("/return")
    void returnProducts(@RequestBody Map<@NotNull UUID, @NotNull @Min(1) Long> returnRequest);
}