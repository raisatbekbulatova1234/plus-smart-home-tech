package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.facade.OrderFacade;
import ru.yandex.practicum.utils.PaginationConstants;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * REST контроллер для управления заказами
 * Реализует интерфейс OrderClient для использования через Feign
 *
 * Эндпоинты:
 * - PUT /api/v1/order - создание заказа из корзины
 * - GET /api/v1/order - получение заказов пользователя
 * - POST /api/v1/order/payment - подготовка к оплате
 * - POST /api/v1/order/payment/successful - успешная оплата
 * - POST /api/v1/order/payment/failed - ошибка оплаты
 * - POST /api/v1/order/assembly - успешная сборка
 * - POST /api/v1/order/assembly/failed - ошибка сборки
 * - POST /api/v1/order/delivery - подготовка доставки
 * - POST /api/v1/order/delivery/successful - успешная доставка
 * - POST /api/v1/order/delivery/failed - ошибка доставки
 * - POST /api/v1/order/completed - завершение заказа
 * - POST /api/v1/order/return - возврат товара
 * - POST /api/v1/order/calculate/total - расчет полной цены
 * - POST /api/v1/order/calculate/delivery - расчет цены доставки
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderFacade orderFacade;

    // ==================== СОЗДАНИЕ ЗАКАЗА ====================

    /**
     * PUT /api/v1/order
     * Создание нового заказа из корзины пользователя
     */
    @PutMapping
    public OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest request) {
        log.info("Получен PUT-запрос на создание заказа из корзины с id {}.",
                request.getShoppingCartDto().getShoppingCartId());
        return orderFacade.createOrder(request);
    }

    /**
     * GET /api/v1/order?username=user@example.com
     * Получение всех заказов пользователя с пагинацией
     */
    @GetMapping
    public Page<OrderDto> getOrders(
            @RequestParam String username,
            @PageableDefault(
                    size = PaginationConstants.DEFAULT_PAGE_SIZE,
                    sort = PaginationConstants.ORDER_STATE_SORT,
                    direction = ASC) Pageable pageable) {
        log.info("От пользователя {} получен GET-запрос на просмотр его заказов с пагинацией {}.",
                username, pageable);
        return orderFacade.getUserOrders(username, pageable);
    }

    // ==================== ОПЛАТА ====================

    /**
     * POST /api/v1/order/payment
     * Подготовка заказа к оплате
     */
    @PostMapping("/payment")
    public OrderDto prepareOrderForPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос на оформление оплаты заказа с id {}.", orderId);
        return orderFacade.prepareOrderForPayment(orderId);
    }

    /**
     * POST /api/v1/order/payment/successful
     * Обработка успешной оплаты заказа
     */
    @PostMapping("/payment/successful")
    public OrderDto handleSuccessfulPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об успешной оплате заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulPayment(orderId);
    }

    /**
     * POST /api/v1/order/payment/failed
     * Обработка ошибки оплаты заказа
     */
    @PostMapping("/payment/failed")
    public OrderDto handleFailedPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке оплаты заказа с id {}.", orderId);
        return orderFacade.handleFailedPayment(orderId);
    }

    // ==================== СБОРКА ====================

    /**
     * POST /api/v1/order/assembly
     * Обработка успешной сборки заказа
     */
    @PostMapping("/assembly")
    public OrderDto handleSuccessfulAssembly(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о сборке заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulAssembly(orderId);
    }

    /**
     * POST /api/v1/order/assembly/failed
     * Обработка ошибки сборки заказа
     */
    @PostMapping("/assembly/failed")
    public OrderDto handleFailedAssembly(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке сборки заказа с id {}.", orderId);
        return orderFacade.handleFailedAssembly(orderId);
    }

    // ==================== ДОСТАВКА ====================

    /**
     * POST /api/v1/order/delivery
     * Подготовка доставки заказа
     */
    @PostMapping("/delivery")
    public OrderDto handlePickedDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о подготовке доставки заказа с id {}.", orderId);
        return orderFacade.handlePickedDelivery(orderId);
    }

    /**
     * POST /api/v1/order/delivery/successful
     * Обработка успешной доставки заказа
     */
    @PostMapping("/delivery/successful")
    public OrderDto handleSuccessfulDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о доставке заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulDelivery(orderId);
    }

    /**
     * POST /api/v1/order/delivery/failed
     * Обработка ошибки доставки заказа
     */
    @PostMapping("/delivery/failed")
    public OrderDto handleFailedDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке доставки заказа с id {}.", orderId);
        return orderFacade.handleFailedDelivery(orderId);
    }

    // ==================== ЗАВЕРШЕНИЕ И ВОЗВРАТ ====================

    /**
     * POST /api/v1/order/completed
     * Завершение заказа
     */
    @PostMapping("/completed")
    public OrderDto handleComplete(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о выполнении заказа с id {}.", orderId);
        return orderFacade.handleComplete(orderId);
    }

    /**
     * POST /api/v1/order/return
     * Возврат товара по заказу
     */
    @PostMapping("/return")
    public OrderDto handleReturn(@RequestBody @Valid ProductReturnRequest request) {
        log.info("Получен POST-запрос на возврат товара по заказу с id {}.",
                request.getOrderId());
        return orderFacade.handleReturn(request);
    }

    // ==================== РАСЧЕТЫ ====================

    /**
     * POST /api/v1/order/calculate/total
     * Расчет полной цены заказа (товары + доставка)
     */
    @PostMapping("/calculate/total")
    public OrderDto handleCalculateTotalPrice(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о расчете полной цены заказа с id {}.", orderId);
        return orderFacade.handleCalculateTotalPrice(orderId);
    }

    /**
     * POST /api/v1/order/calculate/delivery
     * Расчет стоимости доставки заказа
     */
    @PostMapping("/calculate/delivery")
    public OrderDto handleCalculateDeliveryPrice(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о расчете цены доставки заказа с id {}.", orderId);
        return orderFacade.handleCalculateDeliveryPrice(orderId);
    }
}