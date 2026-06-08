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

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderClient {
    private final OrderFacade orderFacade;

    @PutMapping
    public OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest request) {
        log.info("Получен PUT-запрос на создание заказа из корзины с id {}.",
                request.getShoppingCartDto().getShoppingCartId());
        return orderFacade.createOrder(request);
    }

    @GetMapping
    public Page<OrderDto> getOrders(@RequestParam String username,
                                    @PageableDefault(
                                            size = PaginationConstants.DEFAULT_PAGE_SIZE,
                                            sort = PaginationConstants.ORDER_STATE_SORT,
                                            direction = ASC) Pageable pageable) {
        log.info("От пользователя {} получен GET-запрос на просмотр его заказов с пагинацией {}.",
                username, pageable);
        return orderFacade.getUserOrders(username, pageable);
    }

    @PostMapping("/payment")
    public OrderDto prepareOrderForPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос на оформление оплаты заказа с id {}.", orderId);
        return orderFacade.prepareOrderForPayment(orderId);
    }

    @PostMapping("/payment/successful")
    public OrderDto handleSuccessfulPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об успешной оплате заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulPayment(orderId);
    }

    @PostMapping("/payment/failed")
    public OrderDto handleFailedPayment(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке оплаты заказа с id {}.", orderId);
        return orderFacade.handleFailedPayment(orderId);
    }

    @PostMapping("/assembly")
    public OrderDto handleSuccessfulAssembly(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о сборке заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulAssembly(orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto handleFailedAssembly(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке сборки заказа с id {}.", orderId);
        return orderFacade.handleFailedAssembly(orderId);
    }

    @PostMapping("/delivery")
    public OrderDto handlePickedDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о подготовке доставки заказа с id {}.", orderId);
        return orderFacade.handlePickedDelivery(orderId);
    }

    @PostMapping("/delivery/successful")
    public OrderDto handleSuccessfulDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о доставке заказа с id {}.", orderId);
        return orderFacade.handleSuccessfulDelivery(orderId);
    }

    @PostMapping("/delivery/failed")
    public OrderDto handleFailedDelivery(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос об ошибке доставки заказа с id {}.", orderId);
        return orderFacade.handleFailedDelivery(orderId);
    }

    @PostMapping("/completed")
    public OrderDto handleComplete(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о выполнении заказа с id {}.", orderId);
        return orderFacade.handleComplete(orderId);
    }

    @PostMapping("/return")
    public OrderDto handleReturn(@RequestBody @Valid ProductReturnRequest request) {
        log.info("Получен POST-запрос на возврат товара по заказу с id {}.",
                request.getOrderId());
        return orderFacade.handleReturn(request);
    }

    @PostMapping("/calculate/total")
    public OrderDto handleCalculateTotalPrice(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о расчете полной цены заказа с id {}.", orderId);
        return orderFacade.handleCalculateTotalPrice(orderId);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto handleCalculateDeliveryPrice(@RequestBody @NotNull UUID orderId) {
        log.info("Получен POST-запрос о расчете цены доставки заказа с id {}.", orderId);
        return orderFacade.handleCalculateDeliveryPrice(orderId);
    }
}
