package ru.yandex.practicum.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.*;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exceptions.order.OrderStateException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.service.OrderService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Реализация фасада заказов
 * Объединяет логику работы с заказами и вызовы внешних сервисов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeImpl implements OrderFacade {

    private final OrderService service;
    private final WarehouseClientOrderFacade warehouseClient;
    private final DeliveryClientOrderFacade deliveryClient;
    private final PaymentClientOrderFacade paymentClient;
    private final OrderMapper mapper;

    // ==================== ОСНОВНЫЕ ОПЕРАЦИИ ====================

    @Override
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        return service.getUserOrders(username, pageable);
    }

    @Override
    public OrderDto createOrder(CreateNewOrderRequest request) {
        return service.createOrder(request);
    }

    /**
     * Обработка возврата товаров
     */
    @Override
    public OrderDto handleReturn(ProductReturnRequest request) {
        log.debug("Facade. Обработка возврата товаров для заказа с id {}.", request.getOrderId());
        UUID orderId = request.getOrderId();
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        // Проверка: заказ должен быть выполнен
        if (state != OrderState.COMPLETED) {
            throw new OrderStateException("Заказ с id " + orderId + " не выполнен. Возврат невозможен.");
        }

        // Возврат товаров на склад
        warehouseClient.returnProducts(request.getProducts());

        // Обновление статуса заказа
        orderContext = orderContext.toBuilder()
                .state(OrderState.PRODUCT_RETURNED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Возврат товаров для заказа с id {} успешно выполнен.", orderId);
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== ПОДГОТОВКА К ОПЛАТЕ ====================

    /**
     * Полный цикл подготовки заказа к оплате:
     * 1. Обогащение данными со склада (вес, объем, хрупкость)
     * 2. Получение адреса склада
     * 3. Регистрация доставки
     * 4. Расчет стоимости доставки
     * 5. Расчет стоимости товаров
     * 6. Расчет полной цены
     * 7. Регистрация платежа
     */
    @Override
    public OrderDto prepareOrderForPayment(UUID orderId) {
        log.debug("Facade. Обработка запроса на подготовку заказа с id {} к оплате.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);

        // Этапы подготовки (каждый проверяет и заполняет недостающие данные)
        orderContext = enrichWeightVolumeFragile(orderContext);  // 1. Данные со склада
        orderContext = enrichFromAddress(orderContext);          // 2. Адрес склада
        orderContext = registerDelivery(orderContext);           // 3. Регистрация доставки
        orderContext = calculateDeliveryPrice(orderContext);     // 4. Стоимость доставки
        orderContext = calculateProductPrice(orderContext);      // 5. Стоимость товаров
        orderContext = calculateTotalPrice(orderContext);        // 6. Полная цена
        orderContext = registerPayment(orderContext);            // 7. Регистрация платежа

        // Обновление статуса
        orderContext = orderContext.toBuilder()
                .state(OrderState.ON_PAYMENT)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Подготовка заказа с id {} к оплате успешно завершена.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== ОПЛАТА ====================

    @Override
    public OrderDto handleSuccessfulPayment(UUID orderId) {
        log.debug("Facade. Обработка успешной оплаты заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        // Проверка: заказ должен быть в статусе "на оплате"
        if (state != OrderState.ON_PAYMENT) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не прошел предварительную процедуру подготовки к оплате.");
        }

        // Уведомление сервиса оплаты
        paymentClient.handleSuccessfulPayment(orderContext.getPaymentId());

        // Обновление статуса
        orderContext = orderContext.toBuilder()
                .state(OrderState.PAID)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Оплата заказа с id {} успешно подтверждена.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    public OrderDto handleFailedPayment(UUID orderId) {
        log.debug("Facade. Обработка неуспешной оплаты заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.ON_PAYMENT) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не прошел предварительную процедуру подготовки к оплате.");
        }

        paymentClient.handleFailedPayment(orderContext.getPaymentId());

        orderContext = orderContext.toBuilder()
                .state(OrderState.PAYMENT_FAILED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Неуспешная оплата заказа с id обработана {}.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== СБОРКА ====================

    @Override
    public OrderDto handleSuccessfulAssembly(UUID orderId) {
        log.debug("Facade. Обработка успешной сборки заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.PAID) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не оплачен, успешная сборка невозможна.");
        }

        orderContext = orderContext.toBuilder()
                .state(OrderState.ASSEMBLED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Заказ с id {} успешно собран.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    public OrderDto handleFailedAssembly(UUID orderId) {
        log.debug("Facade. Обработка неуспешной сборки заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.PAID) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не оплачен, неуспешная сборка невозможна.");
        }

        orderContext = orderContext.toBuilder()
                .state(OrderState.ASSEMBLY_FAILED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Неуспешная сборка заказа с id {} обработана.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== ДОСТАВКА ====================

    @Override
    public OrderDto handlePickedDelivery(UUID orderId) {
        log.debug("Facade. Обработка передачи заказа с id {} в доставку.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.ASSEMBLED) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не собран, подготовка к доставке невозможна.");
        }

        deliveryClient.handlePickedDelivery(orderContext.getDeliveryId());

        orderContext = orderContext.toBuilder()
                .state(OrderState.ON_DELIVERY)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Заказ с id {} передан в доставку.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    public OrderDto handleSuccessfulDelivery(UUID orderId) {
        log.debug("Facade. Обработка успешной доставки заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.ON_DELIVERY) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не находится в доставке, успешное завершение доставки невозможно.");
        }

        deliveryClient.handleSuccessfulDelivery(orderContext.getDeliveryId());

        orderContext = orderContext.toBuilder()
                .state(OrderState.DELIVERED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Доставка заказа с id {} успешно завершена.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    public OrderDto handleFailedDelivery(UUID orderId) {
        log.debug("Facade. Обработка неуспешной доставки заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.ON_DELIVERY) {
            throw new OrderStateException("Заказ с id " + orderId +
                    " не находится в доставке, неуспешная доставка невозможна.");
        }

        deliveryClient.handleFailedDelivery(orderContext.getDeliveryId());

        orderContext = orderContext.toBuilder()
                .state(OrderState.DELIVERY_FAILED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Неуспешная доставка заказа с id {} обработана.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== ЗАВЕРШЕНИЕ ====================

    @Override
    public OrderDto handleComplete(UUID orderId) {
        log.debug("Facade. Обработка завершения заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);
        OrderState state = orderContext.getState();

        if (state != OrderState.DELIVERED) {
            throw new OrderStateException("У заказ с id " + orderId +
                    " не выполнена доставка, завершение заказа невозможно.");
        }

        orderContext = orderContext.toBuilder()
                .state(OrderState.COMPLETED)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Заказ с id {} успешно завершен.", orderContext.getOrderId());
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== РАСЧЕТЫ ====================

    @Override
    public OrderDto handleCalculateTotalPrice(UUID orderId) {
        log.debug("Facade. Обработка запроса на расчет полной цены для заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);

        OrderDtoPayment request = mapper.toOrderDtoPayment(orderContext);
        BigDecimal totalPrice = paymentClient.calculateTotalPrice(request);

        orderContext = orderContext.toBuilder()
                .totalPrice(totalPrice)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Запрос на расчет полной цены для заказа с id {} выполнен, цена {}.",
                orderContext.getOrderId(), totalPrice);
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    public OrderDto handleCalculateDeliveryPrice(UUID orderId) {
        log.debug("Facade. Обработка запроса на расчет цены доставки для заказа с id {}.", orderId);
        OrderContext orderContext = service.getOrderContext(orderId);

        OrderDtoDelivery request = mapper.toOrderDtoDelivery(orderContext);
        BigDecimal deliveryPrice = deliveryClient.calculateDeliveryCost(request);

        orderContext = orderContext.toBuilder()
                .deliveryPrice(deliveryPrice)
                .build();

        Order savedOrder = service.updateOrder(orderContext);

        log.info("Facade. Запрос на расчет цены доставки для заказа с id {} выполнен, цена {}.",
                orderContext.getOrderId(), deliveryPrice);
        return mapper.toOrderDto(savedOrder);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Обогащение заказа данными со склада (вес, объем, хрупкость)
     */
    private OrderContext enrichWeightVolumeFragile(OrderContext orderContext) {
        Double deliveryWeight = orderContext.getDeliveryWeight();
        Double deliveryVolume = orderContext.getDeliveryVolume();
        Boolean fragile = orderContext.getFragile();

        if (deliveryWeight == null || deliveryVolume == null || fragile == null) {
            log.debug("Facade. Подготовлен запрос на склад для обогащения заказа с id {}.", orderContext.getOrderId());

            AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                    .products(orderContext.getProducts())
                    .orderId(orderContext.getOrderId())
                    .build();

            BookedProductsDto response = warehouseClient.assembleOrder(request);

            orderContext = orderContext.toBuilder()
                    .deliveryWeight(response.getDeliveryWeight())
                    .deliveryVolume(response.getDeliveryVolume())
                    .fragile(response.getFragile())
                    .build();

            log.debug("Данные заказа с id {} успешно обновлены.", orderContext.getOrderId());
        }

        return orderContext;
    }

    /**
     * Установка адреса склада (откуда будет отправлен заказ)
     */
    private OrderContext enrichFromAddress(OrderContext orderContext) {
        AddressDto fromAddress = orderContext.getFromAddress();

        if (fromAddress == null || fromAddress.getStreet() == null || fromAddress.getStreet().isBlank()) {
            log.debug("Facade. Подготовлен запрос по установке адреса склада заказа с id {}.", orderContext.getOrderId());
            fromAddress = warehouseClient.getAddress();

            orderContext = orderContext.toBuilder()
                    .fromAddress(fromAddress)
                    .build();

            log.debug("Facade. Адреса склада для заказа с id {} установлен.", orderContext.getOrderId());
        }

        return orderContext;
    }

    /**
     * Регистрация доставки в сервисе доставки
     */
    private OrderContext registerDelivery(OrderContext orderContext) {
        if (orderContext.getDeliveryId() == null) {
            log.debug("Facade. Подготовлен запрос по регистрации доставки для заказа с id {}.",
                    orderContext.getOrderId());

            NewDeliveryDto dto = mapper.toNewDeliveryDto(orderContext);
            DeliveryDto response = deliveryClient.createDelivery(dto);

            orderContext = orderContext.toBuilder()
                    .deliveryId(response.getDeliveryId())
                    .build();

            log.debug("Facade. Запрос по регистрации доставки для заказа с id {} обработан, доставке присвоен id {}.",
                    orderContext.getOrderId(), response.getDeliveryId());
        }

        return orderContext;
    }

    /**
     * Расчет стоимости доставки
     */
    private OrderContext calculateDeliveryPrice(OrderContext orderContext) {
        if (orderContext.getDeliveryPrice() == null) {
            log.debug("Facade. Подготовлен запрос расчета цены доставки для заказа с id {}.",
                    orderContext.getOrderId());

            OrderDtoDelivery request = mapper.toOrderDtoDelivery(orderContext);
            BigDecimal deliveryPrice = deliveryClient.calculateDeliveryCost(request);

            orderContext = orderContext.toBuilder()
                    .deliveryPrice(deliveryPrice)
                    .build();

            log.debug("Facade. Запрос на расчет цены доставки для заказа с id {} выполнен, цена {}.",
                    orderContext.getOrderId(), deliveryPrice);
        }

        return orderContext;
    }

    /**
     * Расчет стоимости товаров
     */
    private OrderContext calculateProductPrice(OrderContext orderContext) {
        if (orderContext.getProductPrice() == null) {
            log.debug("Facade. Подготовлен запрос расчета цены товаров для заказа с id {}.",
                    orderContext.getOrderId());

            OrderDtoPayment request = mapper.toOrderDtoPayment(orderContext);
            BigDecimal productPrice = paymentClient.calculateProductPrice(request);

            orderContext = orderContext.toBuilder()
                    .productPrice(productPrice)
                    .build();

            log.debug("Facade. Запрос на расчет цены товаров для заказа с id {} выполнен, цена {}.",
                    orderContext.getOrderId(), productPrice);
        }

        return orderContext;
    }

    /**
     * Расчет полной цены (товары + доставка)
     */
    private OrderContext calculateTotalPrice(OrderContext orderContext) {
        if (orderContext.getTotalPrice() == null) {
            log.debug("Facade. Подготовлен запрос расчета полной цены для заказа с id {}.",
                    orderContext.getOrderId());

            OrderDtoPayment request = mapper.toOrderDtoPayment(orderContext);
            BigDecimal totalPrice = paymentClient.calculateTotalPrice(request);

            orderContext = orderContext.toBuilder()
                    .totalPrice(totalPrice)
                    .build();

            log.debug("Facade. Запрос на расчет полной цены для заказа с id {} выполнен, цена {}.",
                    orderContext.getOrderId(), totalPrice);
        }

        return orderContext;
    }

    /**
     * Регистрация платежа в сервисе оплаты
     */
    private OrderContext registerPayment(OrderContext orderContext) {
        if (orderContext.getPaymentId() == null) {
            log.debug("Facade. Подготовлен запрос по регистрации платежа для заказа с id {}.",
                    orderContext.getOrderId());

            OrderDtoPayment request = mapper.toOrderDtoPayment(orderContext);
            PaymentDto response = paymentClient.createPayment(request);

            orderContext = orderContext.toBuilder()
                    .paymentId(response.getPaymentId())
                    .build();

            log.debug("Facade. Запрос по регистрации платежа для заказа с id {} обработан, платежу присвоен id {}.",
                    orderContext.getOrderId(), response.getPaymentId());
        }

        return orderContext;
    }
}