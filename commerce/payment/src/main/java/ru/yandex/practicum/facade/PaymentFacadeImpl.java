package ru.yandex.practicum.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ProductPrice;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация фасада платежей
 * Объединяет бизнес-логику с вызовами внешних сервисов (магазин)
 */
@Service
@RequiredArgsConstructor
public class PaymentFacadeImpl implements PaymentFacade {

    private final PaymentService service;
    private final ShoppingStoreClientPaymentFacade shoppingStoreClient;  // Клиент магазина

    /**
     * Создание платежа
     * Делегирует вызов сервису платежей
     */
    @Override
    public PaymentDto createPayment(OrderDtoPayment request) {
        return service.createPayment(request);
    }

    /**
     * Расчет полной стоимости заказа
     * Делегирует вызов сервису платежей
     */
    @Override
    public BigDecimal calculateTotalPrice(OrderDtoPayment request) {
        return service.calculateTotalPrice(request);
    }

    /**
     * Расчет стоимости товаров
     * Получает актуальные цены из сервиса магазина,
     * затем делегирует расчет сервису платежей
     */
    @Override
    public BigDecimal calculateProductPrice(OrderDtoPayment request) {
        // Получаем карту товаров (productId -> количество)
        Map<UUID, Long> productDtoMap = request.getProducts();

        // Для каждого товара получаем актуальную цену из магазина
        List<ProductPrice> itemsWithPrice = productDtoMap.entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Long quantity = entry.getValue();

                    // Запрос к сервису магазина для получения цены товара
                    BigDecimal price = shoppingStoreClient.getProductById(productId).getPrice();

                    // Создание DTO с ценой и количеством
                    return new ProductPrice(productId, quantity, price);
                })
                .toList();

        // Делегируем расчет сервису платежей
        return service.calculateProductPrice(itemsWithPrice);
    }

    /**
     * Обработка успешного платежа
     * Делегирует вызов сервису платежей
     */
    @Override
    public void handleSuccessfulPayment(UUID paymentId) {
        service.handleSuccessfulPayment(paymentId);
    }

    /**
     * Обработка неуспешного платежа
     * Делегирует вызов сервису платежей
     */
    @Override
    public void handleFailedPayment(UUID paymentId) {
        service.handleFailedPayment(paymentId);
    }
}