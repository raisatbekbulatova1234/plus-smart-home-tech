package ru.yandex.practicum.dto.cart;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class ShoppingCartDto {
    UUID shoppingCartId;
    Map<UUID, Long> products;
}