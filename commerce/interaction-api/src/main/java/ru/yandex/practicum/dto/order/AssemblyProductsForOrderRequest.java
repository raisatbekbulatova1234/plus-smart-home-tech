package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class AssemblyProductsForOrderRequest {
    @NotNull
    @NotEmpty
    Map<@NotNull UUID, @Min(1) Long> products;

    @NotNull
    UUID orderId;
}
