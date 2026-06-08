package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

/**
 * DTO для запроса на сборку заказа на складе
 * Используется при бронировании товаров в сервисе склада
 */
@Value
@Builder
public class AssemblyProductsForOrderRequest {


    @NotNull(message = "Список товаров не может быть null")
    @NotEmpty(message = "Список товаров не может быть пустым")
    Map<@NotNull UUID, @Min(1) Long> products;

    @NotNull(message = "ID заказа не может быть null")
    UUID orderId;
}