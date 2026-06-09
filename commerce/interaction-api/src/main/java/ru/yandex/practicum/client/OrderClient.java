package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.utils.PaginationConstants;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    // Создание заказа
    @PutMapping
    OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest request);

    // Получение заказов
    @GetMapping
    Page<OrderDto> getOrders(@RequestParam String username,
                             @PageableDefault(
                                     size = PaginationConstants.DEFAULT_PAGE_SIZE,
                                     sort = PaginationConstants.ORDER_STATE_SORT,
                                     direction = ASC) Pageable pageable);

    // Цикл обработки заказа

    // 1. Оплата

    /**
     * Перевод заказ в состояние оплаты. Проведение обогащения заказа необходимыми данными.
     * Переводит заказ в статус ON_PAYMENT.
     */
    @PostMapping("/payment")
    OrderDto prepareOrderForPayment(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение успешной или неуспешной оплаты, если статус заказа ON_PAYMENT.
     * Переводит заказ в статус PAID или PAYMENT_FAILED.
     */
    @PostMapping("/payment/successful")
    OrderDto handleSuccessfulPayment(@RequestBody @NotNull UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto handleFailedPayment(@RequestBody @NotNull UUID orderId);

    // 2. Сборка

    /**
     * Подтверждение успешной или неуспешной сборки, если статус заказа PAID.
     * Переводит заказ в статус  ASSEMBLED или ASSEMBLY_FAILED.
     */
    @PostMapping("/assembly")
    OrderDto handleSuccessfulAssembly(@RequestBody @NotNull UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto handleFailedAssembly(@RequestBody @NotNull UUID orderId);

    // 3. Доставка

    /**
     * Регистрация доставки и создание брони на доставку на складе, если статус заказа ASSEMBLED.
     * Переводит заказ в статус  ON_DELIVERY.
     */
    @PostMapping("/delivery")
    OrderDto handlePickedDelivery(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение успешной или неуспешной доставки, если статус заказа ON_DELIVERY.
     * Переводит заказ в статус DELIVERED или DELIVERY_FAILED.
     */
    @PostMapping("/delivery/successful")
    OrderDto handleSuccessfulDelivery(@RequestBody @NotNull UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto handleFailedDelivery(@RequestBody @NotNull UUID orderId);

    // 4. Завершение заказа

    /**
     * Подтверждение завершения заказа, если статус заказа DELIVERED.
     * Переводит заказ в статус COMPLETED.
     */
    @PostMapping("/completed")
    OrderDto handleComplete(@RequestBody @NotNull UUID orderId);

    // 5. Возврат завершенного заказа (по требованию)

    /**
     * Возврат заказа, если статус заказа COMPLETED.
     * Переводит заказ в статус RETURNED.
     */
    @PostMapping("/return")
    OrderDto handleReturn(@RequestBody @Valid ProductReturnRequest request);

    // Расчет полной цены и цены доставки (вспомогательные методы)
    @PostMapping("/calculate/total")
    OrderDto handleCalculateTotalPrice(@RequestBody @NotNull UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto handleCalculateDeliveryPrice(@RequestBody @NotNull UUID orderId);
}
