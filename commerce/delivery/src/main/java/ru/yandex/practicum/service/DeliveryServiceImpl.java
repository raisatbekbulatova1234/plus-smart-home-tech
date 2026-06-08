package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.enums.WarehouseAddresses;
import ru.yandex.practicum.exceptions.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.exceptions.delivery.UnknownWarehouseException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса доставки
 * Содержит бизнес-логику создания и управления доставкой
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryPriceCalculator calculator;

    // ==================== СОЗДАНИЕ ДОСТАВКИ ====================

    /**
     * Создание новой доставки для заказа
     */
    @Override
    @Transactional
    public DeliveryDto createDelivery(NewDeliveryDto request) {
        log.debug("Service. Обработка запроса на регистрацию доставки для заказа с id {}.", request.getOrderId());

        // Поиск или создание адресов
        Address toAddress = findAddressOrCreate(request.getToAddress());      // Адрес доставки
        Address fromAddress = findAddressOrCreate(request.getFromAddress());  // Адрес склада

        // Маппинг DTO → Entity
        Delivery delivery = deliveryMapper.toDelivery(request);
        delivery.setToAddress(toAddress);
        delivery.setFromAddress(fromAddress);
        delivery.setDeliveryState(DeliveryState.CREATED);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        log.info("Service. Доставка для заказа с id {} зарегистрирована, присвоен id {}.",
                savedDelivery.getOrderId(), savedDelivery.getDeliveryId());

        return deliveryMapper.toDeliveryDto(savedDelivery);
    }

    // ==================== УПРАВЛЕНИЕ СТАТУСАМИ ====================

    /**
     * Обработка получения доставки (курьер забрал заказ)
     * Обновляет статус на IN_PROGRESS
     */
    @Override
    @Transactional
    public DeliveryDto handlePickedDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию получения товара для доставки с id {}.", deliveryId);

        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        log.info("Service. Запрос на регистрацию получения товара для доставки с id {} обработан.", deliveryId);
        return deliveryMapper.toDeliveryDto(delivery);
    }

    /**
     * Обработка успешной доставки
     * Обновляет статус на DELIVERED
     */
    @Override
    @Transactional
    public void handleSuccessfulDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию успешной доставки с id {}.", deliveryId);

        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);

        log.info("Service. Запрос на регистрацию успешной доставки с id {} обработан.", deliveryId);
    }

    /**
     * Обработка неуспешной доставки
     * Обновляет статус на FAILED
     */
    @Override
    @Transactional
    public void handleFailedDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию неуспешной доставки с id {}.", deliveryId);

        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);

        log.info("Service. Запрос на регистрацию неуспешной доставки с id {} обработан.", deliveryId);
    }

    // ==================== РАСЧЕТ СТОИМОСТИ ====================

    /**
     * Расчет стоимости доставки
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(OrderDtoDelivery request) {
        log.debug("Service. Обработка запроса на расчет цены доставки заказа с id {}.", request.getOrderId());

        // Поиск доставки по ID
        Delivery delivery = findDeliveryOrThrow(request.getDeliveryId());

        // Определение адреса склада из enum
        WarehouseAddresses warehouseAddress = parseWarehouseAddress(delivery.getFromAddress().getStreet());
        String deliveryAddress = delivery.getToAddress().getStreet();

        // Параметры для расчета
        double weight = request.getDeliveryWeight();
        double volume = request.getDeliveryVolume();
        boolean fragile = request.getFragile();

        // Расчет стоимости
        BigDecimal result = calculator.calculateDeliveryPrice(
                warehouseAddress,
                deliveryAddress,
                weight,
                volume,
                fragile);

        log.info("Service. Обработка запроса на расчет цены доставки заказа с id {} проведена, результат: {}.",
                request.getOrderId(), result);
        return result;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Поиск существующего адреса или создание нового
     * Избегает дублирования адресов в БД
     */
    private Address findAddressOrCreate(AddressDto address) {
        // Нормализация flat (защита от null)
        String flat = Optional.ofNullable(address.getFlat()).orElse("");

        // Поиск существующего адреса
        return addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                flat
        ).orElseGet(() -> addressRepository.save(deliveryMapper.toAddress(address)));
    }

    /**
     * Поиск доставки по ID или выбрасывание исключения
     */
    private Delivery findDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findByIdWithAddresses(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка с id: " + deliveryId + " не найдена."));
    }

    /**
     * Парсинг адреса склада в enum
     */
    private WarehouseAddresses parseWarehouseAddress(String address) {
        return Arrays.stream(WarehouseAddresses.values())
                .filter(a -> a.getStreet().equals(address))
                .findFirst()
                .orElseThrow(() -> new UnknownWarehouseException("Неизвестный адрес склада: " + address));
    }
}