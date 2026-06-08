package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductPrice;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.exceptions.payment.OrderValidationException;
import ru.yandex.practicum.exceptions.payment.PaymentNotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса платежей
 * Содержит бизнес-логику создания и обработки платежей
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.1);  // Налоговая ставка 10%

    // ==================== СОЗДАНИЕ ПЛАТЕЖА ====================

    /**
     * Создание нового платежа для заказа
     */
    @Override
    @Transactional
    public PaymentDto createPayment(OrderDtoPayment request) {
        log.debug("Service. Обработка запроса на регистрацию оплаты заказа с id {}.", request.getOrderId());

        // Валидация входных данных
        validateOrderForPriceCalculation(request);

        // Расчет составляющих платежа
        BigDecimal productPrice = request.getProductPrice();
        BigDecimal deliveryTotal = request.getDeliveryPrice();
        BigDecimal feeTotal = calculateFeeTotal(productPrice);  // Налог с цены товаров

        // Создание сущности платежа
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .productPrice(productPrice)
                .deliveryTotal(deliveryTotal)
                .feeTotal(feeTotal)
                .state(PaymentState.PENDING)
                .build();

        Payment savedPayment = repository.save(payment);
        log.debug("Service. Запрос на регистрацию оплаты заказа с id {} обработан.", request.getOrderId());

        return mapper.toPaymentDto(savedPayment);
    }

    // ==================== РАСЧЕТЫ ====================

    /**
     * Расчет полной стоимости заказа (товары + налог + доставка)
     */
    @Override
    public BigDecimal calculateTotalPrice(OrderDtoPayment request) {
        log.debug("Service. Обработка запроса на расчет полной цены заказа с id {}.", request.getOrderId());

        validateOrderForPriceCalculation(request);

        BigDecimal productPrice = request.getProductPrice();
        BigDecimal deliveryPrice = request.getDeliveryPrice();
        BigDecimal feeTotal = calculateFeeTotal(productPrice);  // Налог

        BigDecimal totalPrice = productPrice.add(feeTotal).add(deliveryPrice);

        log.info("Service. Запрос на расчет полной цены заказа с id {} выполнен, полная цена {}.",
                request.getOrderId(), totalPrice);
        return totalPrice;
    }

    /**
     * Расчет стоимости товаров
     * Суммирует цену × количество для каждой позиции
     */
    @Override
    public BigDecimal calculateProductPrice(List<ProductPrice> itemsWithPrice) {
        log.debug("Service. Обработка запроса на расчет цены {} продуктов.", itemsWithPrice.size());

        BigDecimal productPrice = itemsWithPrice.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Service. Запрос на расчет цены продуктов обработан. Итоговая цена: {}.", productPrice);
        return productPrice;
    }

    // ==================== ОБРАБОТКА ПЛАТЕЖЕЙ ====================

    /**
     * Обработка успешного платежа
     * Обновляет статус на SUCCESS
     */
    @Override
    @Transactional
    public void handleSuccessfulPayment(UUID paymentId) {
        log.debug("Service. Обработка запроса на подтверждение успешного платежа с id {}.", paymentId);

        Payment payment = findPaymentOrThrow(paymentId);
        payment.setState(PaymentState.SUCCESS);

        log.info("Service. Запрос на подтверждение успешного платежа с id {} выполнен.", paymentId);
    }

    /**
     * Обработка неуспешного платежа
     * Обновляет статус на FAILED
     */
    @Override
    @Transactional
    public void handleFailedPayment(UUID paymentId) {
        log.debug("Service. Обработка запроса на подтверждение неуспешного платежа с id {}.", paymentId);

        Payment payment = findPaymentOrThrow(paymentId);
        payment.setState(PaymentState.FAILED);

        log.info("Service. Запрос на подтверждение неуспешного платежа с id {} выполнен.", paymentId);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Валидация заказа для расчета цен
     * Проверяет, что цены не null и не отрицательные
     */
    public void validateOrderForPriceCalculation(OrderDtoPayment order) {
        List<String> errors = new ArrayList<>();

        if (order == null) {
            throw new OrderValidationException(List.of("Переданный заказ - null."));
        }

        // Проверка цен
        if (order.getProductPrice() == null || order.getDeliveryPrice() == null) {
            errors.add("Передана null цена товаров или доставки.");
        } else if (order.getProductPrice().compareTo(BigDecimal.ZERO) < 0 ||
                order.getDeliveryPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Цена продуктов или доставки меньше нуля.");
        }

        if (!errors.isEmpty()) {
            throw new OrderValidationException(errors);
        }
    }

    /**
     * Расчет налога (комиссии) от цены товаров
     */
    private BigDecimal calculateFeeTotal(BigDecimal productPrice) {
        return productPrice.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);  // Округление до копеек
    }

    /**
     * Поиск платежа по ID или выбрасывание исключения
     */
    private Payment findPaymentOrThrow(UUID paymentId) {
        return repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Платеж с id: " + paymentId + " не найден."));
    }
}