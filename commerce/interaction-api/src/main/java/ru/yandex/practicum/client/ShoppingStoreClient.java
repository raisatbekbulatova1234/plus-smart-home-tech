package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.utils.PaginationConstants;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Feign клиент для взаимодействия с сервисом магазина (shopping-store)
 *
 * Используется другими микросервисами (корзина, заказы, склад) для:
 * - Получения информации о товарах
 * - Управления каталогом товаров
 * - Проверки состояния товаров
 *
 * name = "shopping-store" - имя сервиса в Eureka (Service Discovery)
 * path = "/api/v1/shopping-store" - базовый путь для всех запросов к сервису
 */
@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                 @PageableDefault(
                                         size = PaginationConstants.DEFAULT_PAGE_SIZE,
                                         sort = PaginationConstants.DEFAULT_SORT,
                                         direction = ASC) Pageable pageable);

    @PutMapping
    ProductDto addProduct(@RequestBody @Valid NewProductDto newProduct);

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid UpdatedProductDto updatedProduct);

    @PostMapping("/removeProductFromStore")
    boolean deactivateProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@RequestParam UUID productId,
                                    @RequestParam QuantityState quantityState);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);
}