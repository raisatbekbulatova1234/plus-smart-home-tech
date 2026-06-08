package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;
import ru.yandex.practicum.facade.DeliveryFacade;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST контроллер для управления доставкой
 * Реализует интерфейс DeliveryClient для использования через Feign
 *
 * Эндпоинты:
 * - PUT /api/v1/delivery - создание доставки
 * - POST /api/v1/delivery/successful - успешная доставка
 * - POST /api/v1/delivery/picked - доставка взята в работу
 * - POST /api/v1/delivery/failed - ошибка доставки
 * - POST /api/v1/delivery/cost - расчет стоимости доставки
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryClient {

    private final DeliveryFacade deliveryFacade;  // Фасад для бизнес-логики доставки

    /**
     * PUT /api/v1/delivery
     * Создание новой доставки для заказа
     */
    @PutMapping
    public DeliveryDto createDelivery(@RequestBody @Valid NewDeliveryDto request) {
        log.info("Получен PUT-запрос на создание доставки заказа с id {}.", request.getOrderId());
        return deliveryFacade.createDelivery(request);
    }

    /**
     * POST /api/v1/delivery/successful
     * Подтверждение успешной доставки
     * Обновляет статус доставки на SUCCESS
     */
    @PostMapping("/successful")
    public void handleSuccessfulDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("Получен POST-запрос на фиксацию успешной доставки с id {}.", deliveryId);
        deliveryFacade.handleSuccessfulDelivery(deliveryId);
    }

    /**
     * POST /api/v1/delivery/picked
     * Подтверждение, что доставка взята в работу (курьер забрал заказ)
     * Обновляет статус доставки на PICKED
     */
    @PostMapping("/picked")
    public void handlePickedDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("Получен POST-запрос на получение товара в доставку с id {}.", deliveryId);
        deliveryFacade.handlePickedDelivery(deliveryId);
    }

    /**
     * POST /api/v1/delivery/failed
     * Подтверждение ошибки доставки
     * Обновляет статус доставки на FAILED
     */
    @PostMapping("/failed")
    public void handleFailedDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("Получен POST-запрос на фиксацию ошибки в доставке с id {}.", deliveryId);
        deliveryFacade.handleFailedDelivery(deliveryId);
    }

    /**
     * POST /api/v1/delivery/cost
     * Расчет стоимости доставки
     * Учитывает вес, объем и хрупкость товаров
     */
    @PostMapping("/cost")
    public BigDecimal calculateDeliveryCost(@RequestBody @Valid OrderDtoDelivery request) {
        log.info("Получен POST-запрос на расчет стоимости доставки заказа с id {}.", request.getOrderId());
        return deliveryFacade.calculateDeliveryCost(request);
    }
}