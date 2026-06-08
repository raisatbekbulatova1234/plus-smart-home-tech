package ru.yandex.practicum.dto.store;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;

@Value
@Builder
public class NewProductDto {
    @NotBlank
    @Size(min = 3, max = 255, message = "Название товара должно содержать от 3 до 255 символов.")
    String productName;

    @NotBlank
    String description;

    @NotBlank
    @Size(min = 3, max = 512, message = "Ссылка на изображение товара должна содержать от 3 до 512 символов.")
    String imageSrc;

    @NotNull
    QuantityState quantityState;

    @NotNull
    ProductState productState;

    @NotNull
    ProductCategory productCategory;

    @NotNull
    @PositiveOrZero
    BigDecimal price;
}
