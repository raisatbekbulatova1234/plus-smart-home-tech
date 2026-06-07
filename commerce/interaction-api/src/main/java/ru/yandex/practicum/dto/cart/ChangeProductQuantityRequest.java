package ru.yandex.practicum.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;
/**
 * DTO для запроса на изменение количества товара в корзине
 * Используется в эндпоинте: POST /api/v1/shopping-cart/change-quantity
 */
@Value
@Builder
public class ChangeProductQuantityRequest {
    @NotNull
    UUID productId;

    @NotNull
    @Min(1)
    Long newQuantity;
}