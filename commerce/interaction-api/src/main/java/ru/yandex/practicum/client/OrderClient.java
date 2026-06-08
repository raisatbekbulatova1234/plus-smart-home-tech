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

/**
 * Feign клиент для взаимодействия с сервисом заказов (Order Service)
 *
 * Используется другими микросервисами (payment, delivery, warehouse) для:
 * - Создания заказов
 * - Управления жизненным циклом заказа
 * - Обработки платежей, сборки, доставки
 *
 * name = "order" - имя сервиса в Eureka (Service Discovery)
 * path = "/api/v1/order" - базовый путь для всех запросов к сервису
 */
@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    // ==================== СОЗДАНИЕ ЗАКАЗА ====================

    /**
     * Создание нового заказа из корзины
     */
    @PutMapping
    OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest request);

    // ==================== ПОЛУЧЕНИЕ ЗАКАЗОВ ====================

    /**
     * Получение заказов пользователя с пагинацией
     */
    @GetMapping
    Page<OrderDto> getOrders(@RequestParam String username,
                             @PageableDefault(
                                     size = PaginationConstants.DEFAULT_PAGE_SIZE,
                                     sort = PaginationConstants.ORDER_STATE_SORT,
                                     direction = ASC) Pageable pageable);

    // ==================== ОПЛАТА ====================

    /**
     * Перевод заказа в состояние оплаты
     * Проводит обогащение заказа необходимыми данными (вес, объем, хрупкость, адрес склада)
     * Переводит заказ в статус ON_PAYMENT
     */
    @PostMapping("/payment")
    OrderDto prepareOrderForPayment(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение успешной оплаты
     * Переводит заказ в статус PAID
     */
    @PostMapping("/payment/successful")
    OrderDto handleSuccessfulPayment(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение неуспешной оплаты
     * Переводит заказ в статус PAYMENT_FAILED
     */
    @PostMapping("/payment/failed")
    OrderDto handleFailedPayment(@RequestBody @NotNull UUID orderId);

    // ==================== СБОРКА ====================

    /**
     * Подтверждение успешной сборки заказа на складе
     * Переводит заказ в статус ASSEMBLED
     */
    @PostMapping("/assembly")
    OrderDto handleSuccessfulAssembly(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение неуспешной сборки заказа
     * Переводит заказ в статус ASSEMBLY_FAILED
     */
    @PostMapping("/assembly/failed")
    OrderDto handleFailedAssembly(@RequestBody @NotNull UUID orderId);

    // ==================== ДОСТАВКА ====================

    /**
     * Регистрация доставки и создание брони на доставку на складе
     * Переводит заказ в статус ON_DELIVERY
     */
    @PostMapping("/delivery")
    OrderDto handlePickedDelivery(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение успешной доставки
     * Переводит заказ в статус DELIVERED
     */
    @PostMapping("/delivery/successful")
    OrderDto handleSuccessfulDelivery(@RequestBody @NotNull UUID orderId);

    /**
     * Подтверждение неуспешной доставки
     * Переводит заказ в статус DELIVERY_FAILED
     */
    @PostMapping("/delivery/failed")
    OrderDto handleFailedDelivery(@RequestBody @NotNull UUID orderId);

    // ==================== ЗАВЕРШЕНИЕ ЗАКАЗА ====================

    /**
     * Подтверждение завершения заказа
     * Переводит заказ в статус COMPLETED
     */
    @PostMapping("/completed")
    OrderDto handleComplete(@RequestBody @NotNull UUID orderId);

    // ==================== ВОЗВРАТ ====================

    /**
     * Возврат завершенного заказа
     * Переводит заказ в статус PRODUCT_RETURNED
     */
    @PostMapping("/return")
    OrderDto handleReturn(@RequestBody @Valid ProductReturnRequest request);

    // ==================== РАСЧЕТЫ ====================

    /**
     * Расчет полной цены заказа
     */
    @PostMapping("/calculate/total")
    OrderDto handleCalculateTotalPrice(@RequestBody @NotNull UUID orderId);

    /**
     * Расчет стоимости доставки
     */
    @PostMapping("/calculate/delivery")
    OrderDto handleCalculateDeliveryPrice(@RequestBody @NotNull UUID orderId);
}