package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityState;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.service.ShoppingStoreService;
import ru.yandex.practicum.utils.PaginationConstants;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ShoppingStoreService shoppingStoreService;

    /**
     * GET /api/v1/shopping-store?category=ELECTRONICS&page=0&size=10
     * Получение списка товаров по категории с пагинацией
     */
    @GetMapping
    public Page<ProductDto> getProducts(
            @RequestParam ProductCategory category,
            @PageableDefault(
                    size = PaginationConstants.DEFAULT_PAGE_SIZE,
                    sort = PaginationConstants.DEFAULT_SORT,
                    direction = ASC) Pageable pageable) {
        log.info("Получен GET-запрос на просмотр товаров категории {} с пагинацией {}.",
                category, pageable);
        return shoppingStoreService.getProductsByCategory(category, pageable);
    }

    /**
     * PUT /api/v1/shopping-store
     * Добавление нового товара в магазин
     */
    @PutMapping
    public ProductDto addProduct(@RequestBody @Valid NewProductDto newProduct) {
        log.info("Получен PUT-запрос на добавление нового товара {}.", newProduct.getProductName());
        return shoppingStoreService.addProduct(newProduct);
    }

    /**
     * POST /api/v1/shopping-store
     * Обновление существующего товара
     */
    @PostMapping
    public ProductDto updateProduct(@RequestBody @Valid UpdatedProductDto updatedProduct) {
        log.info("Получен POST-запрос на обновление данных товара с id {}.",
                updatedProduct.getProductId());
        return shoppingStoreService.updateProduct(updatedProduct);
    }

    /**
     * POST /api/v1/shopping-store/removeProductFromStore
     * Деактивация товара (мягкое удаление)
     */
    @PostMapping("/removeProductFromStore")
    public boolean deactivateProduct(@RequestBody UUID productId) {
        log.info("Получен POST-запрос на удаление товара с productId {} из магазина.", productId);
        return shoppingStoreService.deactivateProduct(productId);
    }

    /**
     * POST /api/v1/shopping-store/quantityState?productId=...&quantityState=...
     * Изменение состояния количества товара (достаточно/недостаточно)
     */
    @PostMapping("/quantityState")
    public boolean setProductQuantityState(
            @RequestParam UUID productId,          // ID товара
            @RequestParam QuantityState quantityState) {  // Новое состояние количества
        log.info("Получен POST-запрос на изменение количества товара с id {} на {}.",
                productId, quantityState);

        // Создаем DTO из параметров запроса
        SetProductQuantityState request = SetProductQuantityState.builder()
                .productId(productId)
                .quantityState(quantityState)
                .build();

        return shoppingStoreService.setProductQuantityState(request);
    }

    /**
     * GET /api/v1/shopping-store/{productId}
     * Получение товара по ID
     */
    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {  // ID из пути URL
        log.info("Получен GET-запрос на просмотр товара с productId {}.", productId);
        return shoppingStoreService.getProductById(productId);
    }
}