package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryClient {
    @PutMapping
    DeliveryDto createDelivery(@RequestBody @Valid NewDeliveryDto request);

    @PostMapping("/successful")
    void handleSuccessfulDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/picked")
    void handlePickedDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/failed")
    void handleFailedDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/cost")
    BigDecimal calculateDeliveryCost(@RequestBody @Valid OrderDtoDelivery request);
}
