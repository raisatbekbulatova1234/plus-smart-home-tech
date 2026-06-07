package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.model.Product;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Product (товары магазина)
 */
public interface ShoppingStoreRepository extends JpaRepository<Product, UUID> {

    /**
     * Поиск товаров по категории с пагинацией
     */
    Page<Product> findByProductCategory(ProductCategory category, Pageable pageable);
}