package ru.yandex.practicum.dto.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.dto.warehouse.AddressDto;

import java.util.UUID;

@Value
@Builder
public class NewDeliveryDto {
    @Valid
    AddressDto fromAddress;

    @Valid
    AddressDto toAddress;

    @NotNull
    UUID orderId;
}
