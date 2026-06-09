package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {
    @PostMapping
    PaymentDto createPayment(@RequestBody @Valid OrderDtoPayment request);

    @PostMapping("/totalCost")
    BigDecimal calculateTotalPrice(@RequestBody @Valid OrderDtoPayment request);

    @PostMapping("/productCost")
    BigDecimal calculateProductPrice(@RequestBody @Valid OrderDtoPayment request);

    @PostMapping("/refund")
    void handleSuccessfulPayment(@RequestBody UUID paymentId);

    @PostMapping("/failed")
    void handleFailedPayment(@RequestBody UUID paymentId);
}
