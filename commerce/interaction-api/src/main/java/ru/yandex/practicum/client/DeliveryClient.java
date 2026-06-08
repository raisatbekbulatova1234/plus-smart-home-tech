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

/**
 * Feign клиент для взаимодействия с сервисом доставки (Delivery Service)
 *
 * Используется сервисом заказов для:
 * - Создания доставки
 * - Управления статусами доставки (успешно/неуспешно/взят в доставку)
 * - Расчет стоимости доставки
 *
 * name = "delivery" - имя сервиса в Eureka (Service Discovery)
 * path = "/api/v1/delivery" - базовый путь для всех запросов к сервису
 */
@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryClient {

    // ==================== СОЗДАНИЕ ДОСТАВКИ ====================

    /**
     * Создание новой доставки для заказа
     */
    @PutMapping
    DeliveryDto createDelivery(@RequestBody @Valid NewDeliveryDto request);

    // ==================== УПРАВЛЕНИЕ СТАТУСАМИ ДОСТАВКИ ====================

    /**
     * Подтверждение успешной доставки
     */
    @PostMapping("/successful")
    void handleSuccessfulDelivery(@RequestBody @NotNull UUID deliveryId);

    /**
     * Подтверждение, что доставка взята в работу (курьер забрал)
     */
    @PostMapping("/picked")
    void handlePickedDelivery(@RequestBody @NotNull UUID deliveryId);

    /**
     * Подтверждение неуспешной доставки (проблемы при доставке)
     */
    @PostMapping("/failed")
    void handleFailedDelivery(@RequestBody @NotNull UUID deliveryId);

    // ==================== РАСЧЕТ СТОИМОСТИ ====================

    /**
     * Расчет стоимости доставки
     */
    @PostMapping("/cost")
    BigDecimal calculateDeliveryCost(@RequestBody @Valid OrderDtoDelivery request);
}