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

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryPriceCalculator calculator;

    @Override
    @Transactional
    public DeliveryDto createDelivery(NewDeliveryDto request) {
        log.debug("Service. Обработка запроса на регистрацию доставки для заказа с id {}.", request.getOrderId());
        Address toAddress = findAddressOrCreate(request.getToAddress());
        Address fromAddress = findAddressOrCreate(request.getFromAddress());

        Delivery delivery = deliveryMapper.toDelivery(request);

        delivery.setToAddress(toAddress);
        delivery.setFromAddress(fromAddress);
        delivery.setDeliveryState(DeliveryState.CREATED);

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Service. Доставка для заказа с id {} зарегистрирована, присвоен id {}.",
                savedDelivery.getOrderId(), savedDelivery.getDeliveryId());
        return deliveryMapper.toDeliveryDto(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDto handlePickedDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию получения товара для доставки с id {}.", deliveryId);
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        log.info("Service. Запрос на регистрацию получения товара для доставки с id {} обработан.", deliveryId);
        return deliveryMapper.toDeliveryDto(delivery);
    }

    @Override
    @Transactional
    public void handleSuccessfulDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию успешной доставки с id {}.", deliveryId);
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        log.info("Service. Запрос на регистрацию успешной доставки с id {} обработан.", deliveryId);
    }

    @Override
    @Transactional
    public void handleFailedDelivery(UUID deliveryId) {
        log.debug("Service. Обработка запроса на регистрацию неуспешной доставки с id {}.", deliveryId);
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        log.info("Service. Запрос на регистрацию неуспешной доставки с id {} обработан.", deliveryId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(OrderDtoDelivery request) {
        log.debug("Service. Обработка запроса на расчет цены доставки заказа с id {}.", request.getOrderId());
        Delivery delivery = findDeliveryOrThrow(request.getDeliveryId());
        
        WarehouseAddresses warehouseAddress = parseWarehouseAddress(delivery.getFromAddress().getStreet());
        String deliveryAddress = delivery.getToAddress().getStreet();
        double weight = request.getDeliveryWeight();
        double volume = request.getDeliveryVolume();
        boolean fragile = request.getFragile();

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

    private Address findAddressOrCreate(AddressDto address) {
        String flat = Optional.ofNullable(address.getFlat())
                .orElse("");

        return addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                flat).orElseGet(() -> addressRepository.save(deliveryMapper.toAddress(address)));
    }

    private Delivery findDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findByIdWithAddresses(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка с id: " + deliveryId + " не найдена."));
    }

    private WarehouseAddresses parseWarehouseAddress(String address) {

        return Arrays.stream(WarehouseAddresses.values())
                .filter(a -> a.getStreet().equals(address))
                .findFirst()
                .orElseThrow(() ->
                        new UnknownWarehouseException("Неизвестный адрес склада: " + address));
    }
}
