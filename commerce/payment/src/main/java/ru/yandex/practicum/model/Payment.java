package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сущность платежа
 * Маппится на таблицу payments в БД
 * Хранит информацию о платеже заказа: стоимость товаров, доставки, комиссии
 */
@Entity
@Table(name = "payments", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id                                 // Первичный ключ
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.UUID)  // Автогенерация UUID
    private UUID paymentId;             // Уникальный ID платежа

    @Column(name = "order_id", nullable = false)  // NOT NULL
    private UUID orderId;               // ID заказа, к которому относится платеж

    @Column(name = "product_price", nullable = false)  // NOT NULL
    private BigDecimal productPrice;    // Стоимость товаров в заказе

    @Column(name = "delivery_total", nullable = false)  // NOT NULL
    private BigDecimal deliveryTotal;   // Стоимость доставки

    @Column(name = "fee_total", nullable = false)  // NOT NULL
    private BigDecimal feeTotal;        // Комиссия за обработку платежа

    @Builder.Default                     // Значение по умолчанию при использовании Builder
    @Enumerated(EnumType.STRING)        // Хранить enum как строку в БД
    @Column(name = "state", nullable = false)
    private PaymentState state = PaymentState.PENDING;  // Статус платежа (PENDING, PAID, FAILED)

    /**
     * Расчет общей суммы платежа
     *
     * @return BigDecimal - сумма товаров + доставка + комиссия
     */
    public BigDecimal getTotalPayment() {
        return productPrice.add(deliveryTotal).add(feeTotal);
    }
}