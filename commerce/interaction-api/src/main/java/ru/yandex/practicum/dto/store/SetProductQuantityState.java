package ru.yandex.practicum.dto.store;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Value
@Builder
public class SetProductQuantityState {
    @NotNull
    UUID productId;

    @NotNull
    QuantityState quantityState;
}
