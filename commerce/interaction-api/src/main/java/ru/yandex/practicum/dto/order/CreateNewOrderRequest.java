package ru.yandex.practicum.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;

@Value
@Builder
public class CreateNewOrderRequest {
    @Valid
    ShoppingCartDto shoppingCartDto;

    @NotBlank
    String username;

    @Valid
    AddressDto addressDto;
}