package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.enums.WarehouseAddresses;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Калькулятор стоимости доставки
 * Рассчитывает цену доставки на основе:
 * - Адреса склада
 * - Адреса доставки
 * - Веса и объема товаров
 * - Хрупкости товаров
 */
@Component
class DeliveryPriceCalculator {

    // ==================== БАЗОВЫЕ СТАВКИ ====================
    private static final BigDecimal BASE_DELIVERY_PRICE = BigDecimal.valueOf(0.5);      // Базовая цена доставки
    private static final BigDecimal ADDRESS_1_MULTIPLIER = BigDecimal.valueOf(1.0);    // Множитель для склада 1
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2.0);    // Множитель для склада 2
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);      // Надбавка за хрупкость (20%)
    private static final BigDecimal WEIGHT_COEFFICIENT = BigDecimal.valueOf(0.3);      // Коэффициент веса
    private static final BigDecimal VOLUME_COEFFICIENT = BigDecimal.valueOf(0.2);      // Коэффициент объема
    private static final BigDecimal NOT_SAME_STREET_MULTIPLIER = BigDecimal.valueOf(0.2); // Надбавка за разные улицы (20%)

    /**
     * Расчет стоимости доставки
     */
    BigDecimal calculateDeliveryPrice(
            WarehouseAddresses warehouseAddress,
            String deliveryAddress,
            double weight,
            double volume,
            boolean fragile
    ) {
        // 1. Базовая цена
        BigDecimal result = BASE_DELIVERY_PRICE;

        // 2. Применяем множитель склада
        result = result.multiply(applyWarehouseMultiplier(warehouseAddress));

        // 3. Надбавка за хрупкость (+20%)
        if (fragile) {
            result = result.add(result.multiply(FRAGILE_MULTIPLIER));
        }

        // 4. Надбавка за вес (вес × 0.3)
        result = result.add(BigDecimal.valueOf(weight).multiply(WEIGHT_COEFFICIENT));

        // 5. Надбавка за объем (объем × 0.2)
        result = result.add(BigDecimal.valueOf(volume).multiply(VOLUME_COEFFICIENT));

        // 6. Надбавка за разные улицы (+20%)
        if (!isSameStreet(warehouseAddress.getStreet(), deliveryAddress)) {
            result = result.add(result.multiply(NOT_SAME_STREET_MULTIPLIER));
        }

        // 7. Округление до 2 знаков (копейки)
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Применение множителя в зависимости от склада
     */
    private BigDecimal applyWarehouseMultiplier(WarehouseAddresses address) {
        return switch (address) {
            case ADDRESS_1 -> ADDRESS_1_MULTIPLIER;  // Склад 1: множитель 1.0
            case ADDRESS_2 -> ADDRESS_2_MULTIPLIER;  // Склад 2: множитель 2.0 (дороже)
        };
    }

    /**
     * Проверка, совпадают ли улицы склада и доставки
     */
    private boolean isSameStreet(String a, String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Адрес улицы не может быть null.");
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }
}