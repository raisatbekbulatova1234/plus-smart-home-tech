package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.enums.WarehouseAddresses;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
class DeliveryPriceCalculator {
    private static final BigDecimal BASE_DELIVERY_PRICE = BigDecimal.valueOf(0.5);
    private static final BigDecimal ADDRESS_1_MULTIPLIER = BigDecimal.valueOf(1.0);
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_COEFFICIENT = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_COEFFICIENT = BigDecimal.valueOf(0.2);
    private static final BigDecimal NOT_SAME_STREET_MULTIPLIER = BigDecimal.valueOf(0.2);

    BigDecimal calculateDeliveryPrice(
            WarehouseAddresses warehouseAddress,
            String deliveryAddress,
            double weight,
            double volume,
            boolean fragile
    ) {
        BigDecimal result = BASE_DELIVERY_PRICE;

        result = result.multiply(applyWarehouseMultiplier(warehouseAddress));

        if (fragile) {
            result = result.add(result.multiply(FRAGILE_MULTIPLIER));
        }

        result = result.add(BigDecimal.valueOf(weight).multiply(WEIGHT_COEFFICIENT));
        result = result.add(BigDecimal.valueOf(volume).multiply(VOLUME_COEFFICIENT));

        if (!isSameStreet(warehouseAddress.getStreet(), deliveryAddress)) {
            result = result.add(result.multiply(NOT_SAME_STREET_MULTIPLIER));
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyWarehouseMultiplier(WarehouseAddresses address) {
        return switch (address) {
            case ADDRESS_1 -> ADDRESS_1_MULTIPLIER;
            case ADDRESS_2 -> ADDRESS_2_MULTIPLIER;
        };
    }

    private boolean isSameStreet(String a, String b) {

        if (a == null || b == null) {
            throw new IllegalArgumentException("Адрес улицы не может быть null.");
        }

        return a.trim().equalsIgnoreCase(b.trim());
    }
}
