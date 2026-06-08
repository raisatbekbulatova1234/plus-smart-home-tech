package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exceptions.warehouse.*;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Реализация сервиса склада
 * Управление остатками, бронирование товаров для заказов, обработка возвратов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;      // Репозиторий товаров
    private final OrderBookingRepository orderBookingRepository; // Репозиторий бронирований
    private final WarehouseMapper mapper;                        // Маппер для преобразований

    // Тестовые адреса склада
    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    // ==================== УПРАВЛЕНИЕ ТОВАРАМИ ====================

    /**
     * Добавление нового товара на склад
     */
    @Override
    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest newProductRequest) {
        UUID productId = newProductRequest.getProductId();
        log.debug("Service. Обработка запроса на добавление нового товара с id {}.", productId);

        throwIfExistsById(productId);  // Проверка, что товар уже есть

        Product product = mapper.toProduct(newProductRequest);
        warehouseRepository.save(product);

        log.info("Service. Новый товар с id {} сохранен на складе.", productId);
    }

    /**
     * Пополнение остатков товара на складе
     */
    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest addProductRequest) {
        UUID productId = addProductRequest.getProductId();
        log.debug("Service. Обработка запроса на прием товара с id {}.", productId);

        Product product = findProductOrElseThrow(productId);
        Long oldQuantity = product.getQuantity();
        product.setQuantity(oldQuantity + addProductRequest.getQuantity());

        log.info("Service. Количество товара с id {} обновлено с: {}, до {}.",
                productId, oldQuantity, product.getQuantity());
    }

    /**
     * Возврат товаров на склад (при отмене заказа или возврате)
     */
    @Override
    @Transactional
    public void returnProducts(Map<UUID, Long> returnRequest) {
        log.debug("Service. Обработка запроса на возврат {} товаров на склад.", returnRequest.size());

        List<Product> products = findProductsOrElseThrow(returnRequest.keySet());

        // Увеличение количества каждого возвращаемого товара
        products.forEach(product ->
                product.setQuantity(
                        product.getQuantity() + returnRequest.get(product.getProductId())));

        log.info("Service. Запрос на возврат товаров на склад обработан.");
    }

    // ==================== ПРОВЕРКА КОРЗИНЫ ====================

    /**
     * Проверка корзины (без резервирования товаров)
     * Используется для предварительной проверки перед оформлением заказа
     */
    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        UUID shoppingCartId = shoppingCart.getShoppingCartId();
        log.debug("Service. Обработка запроса на проверку корзины товаров с id {}.", shoppingCartId);

        Map<UUID, Long> cartProducts = shoppingCart.getProducts();
        Map<UUID, Product> warehouseProductsMap = findProductsOrElseThrow(cartProducts.keySet())
                .stream()
                .collect(toMap(Product::getProductId, Function.identity()));

        // reserveProducts = false - только проверка, без резервирования
        BookedProductsDto order = calculateBookingParameters(cartProducts, warehouseProductsMap, false);

        log.info("Service. Корзина с id {} проверена, сформирован заказ. Вес: {}, объем: {}, хрупкость: {}.",
                shoppingCartId, order.getDeliveryWeight(), order.getDeliveryVolume(), order.getFragile());
        return order;
    }

    // ==================== БРОНИРОВАНИЕ ЗАКАЗА ====================

    /**
     * Сборка заказа (бронирование товаров)
     * Создает бронь, резервирует товары на складе
     */
    @Override
    @Transactional
    public BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest assemblyRequest) {
        log.debug("Service. Обработка запроса на сбор товаров для заказа с id {}.",
                assemblyRequest.getOrderId());

        UUID orderId = assemblyRequest.getOrderId();

        // Проверка: бронь для этого заказа уже существует?
        if (orderBookingRepository.existsById(orderId)) {
            throw new OrderBookingAlreadyExistsException("К заказу с id " + orderId + " уже создана бронь.");
        }

        Map<UUID, Long> orderedProductsMap = assemblyRequest.getProducts();

        // Поиск товаров на складе
        Map<UUID, Product> warehouseProductsMap = findProductsOrElseThrow(orderedProductsMap.keySet())
                .stream()
                .collect(toMap(Product::getProductId, Function.identity()));

        // reserveProducts = true - резервируем товары
        BookedProductsDto dto = calculateBookingParameters(orderedProductsMap, warehouseProductsMap, true);

        // Создание записи о бронировании
        OrderBooking orderBooking = OrderBooking.builder()
                .orderId(orderId)
                .deliveryWeight(dto.getDeliveryWeight())
                .deliveryVolume(dto.getDeliveryVolume())
                .fragile(dto.getFragile())
                .build();

        orderBookingRepository.save(orderBooking);

        log.info("Service. Запрос на сбор товаров для заказа с id {} обработан.", orderId);
        return dto;
    }

    /**
     * Подтверждение отгрузки товаров в доставку
     * Обновляет бронь: добавляет ID доставки
     */
    @Override
    @Transactional
    public void shipProductsToDelivery(ShippedToDeliveryRequest shippedRequest) {
        log.debug("Service. Обработка запроса на передачу товаров на доставку для заказа с id {}.",
                shippedRequest.getOrderId());

        UUID orderId = shippedRequest.getOrderId();

        OrderBooking orderBooking = orderBookingRepository.findById(orderId)
                .orElseThrow(() -> new OrderBookingNotFoundException("Бронь для заказа с id: " + orderId + " не найдена."));

        orderBooking.setDeliveryId(shippedRequest.getDeliveryId());

        log.info("Service. Запрос на передачу товаров на доставку для заказа с id {} обработан.", orderId);
    }

    // ==================== АДРЕС СКЛАДА ====================

    /**
     * Получение адреса склада
     */
    @Override
    public AddressDto getAddress() {
        log.info("Service. Обработка запроса на просмотр адреса склада.");

        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Расчет параметров заказа (вес, объем, хрупкость)
     *
     * @param requestedProducts Запрашиваемые товары
     * @param warehouseProductsMap Карта товаров на складе
     * @param reserveProducts Флаг: резервировать ли товары
     * @return DTO с параметрами заказа
     */
    private BookedProductsDto calculateBookingParameters(Map<UUID, Long> requestedProducts,
                                                         Map<UUID, Product> warehouseProductsMap,
                                                         boolean reserveProducts) {
        double deliveryWeight = 0;
        double deliveryVolume = 0;
        boolean fragile = false;
        List<UUID> lowQuantityProducts = new ArrayList<>();

        for (Map.Entry<UUID, Long> order : requestedProducts.entrySet()) {
            UUID productId = order.getKey();
            Long orderedQuantity = order.getValue();

            Product product = warehouseProductsMap.get(productId);
            Long availableQuantity = product.getQuantity();

            // Проверка достаточности количества
            if (availableQuantity < orderedQuantity) {
                log.debug("На складе не хватает товара с id: {}. Нужно {}, есть {}.",
                        productId, orderedQuantity, availableQuantity);
                lowQuantityProducts.add(productId);
            }

            // Резервирование товаров (уменьшение количества)
            if (reserveProducts) {
                product.setQuantity(availableQuantity - orderedQuantity);
            }

            // Накопление веса и объема
            deliveryWeight += product.getWeight() * orderedQuantity;
            deliveryVolume += product.getVolume() * orderedQuantity;

            // Проверка на хрупкость
            if (Boolean.TRUE.equals(product.getFragile())) {
                fragile = true;
            }
        }

        // Если есть товары с недостаточным количеством - исключение
        if (!lowQuantityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "На складе не достаточно следующих товаров: " + lowQuantityProducts);
        }

        return BookedProductsDto.builder()
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .fragile(fragile)
                .build();
    }

    /**
     * Проверка, что товара с указанным ID нет на складе
     */
    private void throwIfExistsById(UUID productId) {
        if (warehouseRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Товар с id: " + productId + " уже есть на складе.");
        }
    }

    /**
     * Поиск товаров на складе по списку ID
     * Если каких-то товаров нет - выбрасывает исключение
     */
    private List<Product> findProductsOrElseThrow(Set<UUID> productIds) {
        List<Product> products = warehouseRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            Set<UUID> foundIds = products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = new HashSet<>(productIds);
            missingIds.removeAll(foundIds);

            throw new NoSpecifiedProductInWarehouseException(
                    "На складе отсутствуют товары с id: " + missingIds);
        }

        return products;
    }

    /**
     * Поиск товара на складе по ID
     */
    private Product findProductOrElseThrow(UUID productId) {
        return warehouseRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Товара с id: " + productId + " нет на складе."));
    }
}