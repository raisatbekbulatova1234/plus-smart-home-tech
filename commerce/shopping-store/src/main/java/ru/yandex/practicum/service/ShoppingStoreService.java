package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityState;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.enums.ProductCategory;

import java.util.UUID;

public interface ShoppingStoreService {
    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    ProductDto addProduct(NewProductDto newProduct);

    ProductDto updateProduct(UpdatedProductDto updatedProduct);

    boolean deactivateProduct(UUID productId);

    boolean setProductQuantityState(SetProductQuantityState quantityState);

    ProductDto getProductById(UUID productId);
}
