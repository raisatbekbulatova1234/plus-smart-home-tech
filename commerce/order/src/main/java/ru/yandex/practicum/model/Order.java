package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Сущность заказа
 * Маппится на таблицу orders в БД
 */
@Entity
@Table(name = "orders", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(name = "shopping_cart_id", nullable = false)
    private UUID shoppingCartId;        // ID корзины, из которой создан заказ

    @Column(name = "username", nullable = false)
    private String username;            // Имя пользователя, оформившего заказ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_address", nullable = false)
    private Address toAddress;          // Адрес доставки

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_address")
    private Address fromAddress;        // Адрес отправления (склад)

    /**
     * Товары в заказе
     * Key: UUID товара (productId)
     * Value: Long количество товара
     */
    @ElementCollection(fetch = FetchType.LAZY)  // Коллекция не-Entity
    @CollectionTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id")          // Колонка для ключа Map
    @Column(name = "quantity")                 // Колонка для значения
    @Builder.Default
    private Map<UUID, Long> products = new HashMap<>();

    @Column(name = "payment_id")
    private UUID paymentId;             // ID платежа (из сервиса оплаты)

    @Column(name = "delivery_id")
    private UUID deliveryId;            // ID доставки (из сервиса доставки)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state = OrderState.NEW;  // Статус заказа (NEW, PAID, DELIVERED и т.д.)

    @Column(name = "delivery_weight")
    private Double deliveryWeight;      // Общий вес заказа (кг)

    @Column(name = "delivery_volume")
    private Double deliveryVolume;      // Общий объем заказа (м³)

    @Column(name = "fragile")
    private Boolean fragile;            // Есть ли хрупкие товары

    @Column(name = "total_price")
    private BigDecimal totalPrice;      // Итоговая цена (товары + доставка)

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;   // Стоимость доставки

    @Column(name = "product_price")
    private BigDecimal productPrice;    // Стоимость товаров без доставки
}