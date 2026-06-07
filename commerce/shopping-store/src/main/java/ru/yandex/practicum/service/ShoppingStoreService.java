package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityState;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.enums.ProductCategory;

import java.util.UUID;

/**
 * Интерфейс сервиса для управления товарами в магазине
 * Определяет бизнес-логику работы с продуктами
 */
public interface ShoppingStoreService {

    /**
     * Получение списка товаров по категории с пагинацией
     */
    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    /**
     * Добавление нового товара в магазин
     */
    ProductDto addProduct(NewProductDto newProduct);

    /**
     * Обновление существующего товара
     * Поддерживает частичное обновление (только переданные поля)
     */
    ProductDto updateProduct(UpdatedProductDto updatedProduct);

    /**
     * Деактивация товара (мягкое удаление)
     * Товар не удаляется из БД, а меняет статус на DEACTIVATED
     */
    boolean deactivateProduct(UUID productId);

    /**
     * Изменение состояния количества товара
     */
    boolean setProductQuantityState(SetProductQuantityState quantityState);

    /**
     * Получение товара по ID
     */
    ProductDto getProductById(UUID productId);
}