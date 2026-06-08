package ru.yandex.practicum.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPrice(UUID productId, Long quantity, BigDecimal price) {}