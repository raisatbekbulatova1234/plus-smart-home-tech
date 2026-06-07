package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сущность товара в магазине
 * Маппится на таблицу shopping_store_products в БД
 */
@Entity
@Table(name = "shopping_store_products", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_src", nullable = false)
    private String imageSrc;      // Ссылка на изображение товара

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state", nullable = false)
    private QuantityState quantityState;  // Состояние остатка товара (например, достаточное/недостаточное)

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state", nullable = false)
    private ProductState productState;    // Состояние товара (например, активен/деактивирован)

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    private ProductCategory productCategory;  // Категория товара (например, электроника, одежда)

    @Column(name = "price", nullable = false)
    private BigDecimal price;      // Цена товара (BigDecimal для точности работы с деньгами)
}