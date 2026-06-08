package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderContext;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exceptions.order.NoOrderFoundException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса заказов
 * Содержит бизнес-логику создания и управления заказами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;      // Репозиторий заказов
    private final AddressRepository addressRepository;  // Репозиторий адресов
    private final OrderMapper mapper;                   // Маппер для преобразований

    /**
     * Получение заказов пользователя с пагинацией
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        log.debug("Service. Обработка запроса на просмотр заказов пользователя {} с пагинация {}.",
                username, pageable);

        Page<Order> searchResult = orderRepository.findByUsername(username, pageable);

        log.info("Service. Запрос на просмотр товаров пользователя {} обработан. Количество найденных записей: {}",
                username, searchResult.getContent().size());

        return searchResult.map(mapper::toOrderDto);
    }

    /**
     * Создание нового заказа из корзины
     */
    @Override
    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest request) {
        log.debug("Service. Обработка запроса на создание заказа из корзины с id {}.",
                request.getShoppingCartDto().getShoppingCartId());

        ShoppingCartDto shoppingCart = request.getShoppingCartDto();
        String username = request.getUsername();

        // Поиск существующего адреса или создание нового
        Address toAddress = findAddressOrCreate(request.getAddressDto());

        // Создание заказа через Builder
        Order order = Order.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .username(username)
                .toAddress(toAddress)
                .products(shoppingCart.getProducts())
                .state(OrderState.NEW)          // Начальный статус
                .build();

        Order savedOrder = orderRepository.save(order);

        log.info("Service. Запрос на создание заказа из корзины с id {} выполнен, создан заказ с id {}.",
                shoppingCart.getShoppingCartId(), savedOrder.getOrderId());

        return mapper.toOrderDto(savedOrder);
    }

    /**
     * Получение контекста заказа для других сервисов (доставка, оплата)
     */
    @Override
    public OrderContext getOrderContext(UUID orderId) {
        return mapper.toOrderContext(findOrderOrThrow(orderId));
    }

    /**
     * Обновление заказа из контекста
     * Используется для обновления полей заказа из других сервисов
     */
    @Override
    @Transactional
    public Order updateOrder(OrderContext orderContext) {
        // Находим заказ
        Order order = findOrderOrThrow(orderContext.getOrderId());

        // Поиск или создание адреса отправления (склад)
        Address fromAddress = findAddressOrCreate(orderContext.getFromAddress());
        order.setFromAddress(fromAddress);

        // Частичное обновление полей заказа из контекста
        mapper.updateOrderFromContext(orderContext, order);

        return order;
    }

    /**
     * Поиск заказа по ID или выбрасывание исключения
     */
    public Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id: " + orderId + " не найден."));
    }

    /**
     * Поиск существующего адреса или создание нового
     * Избегает дублирования адресов в БД
     */
    private Address findAddressOrCreate(AddressDto address) {
        // Нормализация flat (защита от null)
        String flat = Optional.ofNullable(address.getFlat())
                .orElse("");

        // Поиск существующего адреса
        return addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                flat
        ).orElseGet(() -> {
            // Адрес не найден - создаем новый
            return addressRepository.save(mapper.toAddress(address));
        });
    }
}