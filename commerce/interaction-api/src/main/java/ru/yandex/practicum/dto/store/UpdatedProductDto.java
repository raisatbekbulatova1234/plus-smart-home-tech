package ru.yandex.practicum.dto.store;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class UpdatedProductDto {
    @NotNull
    UUID productId;
    String productName;
    String description;
    String imageSrc;
    QuantityState quantityState;
    ProductState productState;
    ProductCategory productCategory;
    BigDecimal price;
}
