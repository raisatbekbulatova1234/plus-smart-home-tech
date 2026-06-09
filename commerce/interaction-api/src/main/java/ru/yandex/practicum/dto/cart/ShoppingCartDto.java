package ru.yandex.practicum.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class ShoppingCartDto {
    @NotNull
    UUID shoppingCartId;

    @NotNull
    @NotEmpty
    Map<@NotNull UUID, @Min(1) Long> products;
}
