package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exceptions.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.warehouse.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest newProductRequest) {
        UUID productId = newProductRequest.getProductId();
        log.debug("Service. Обработка запроса на добавление нового товара с id {}.", productId);
        throwIfExistsById(productId);
        Product product = mapper.toProduct(newProductRequest);
        repository.save(product);
        log.info("Service. Новый товар с id {} сохранен на складе.", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        UUID shoppingCartId = shoppingCart.getShoppingCartId();
        log.debug("Service. Обработка запроса на проверку корзины товаров с id {}.", shoppingCartId);
        Map<UUID, Long> cartProducts = shoppingCart.getProducts();
        Map<UUID, Product> warehouseProductsMap = findProductsOrElseThrow(cartProducts.keySet())
                .stream()
                .collect(toMap(Product::getProductId, Function.identity()));
        BookedProductsDto order = calculateBookingParameters(cartProducts, warehouseProductsMap);
        log.info("Service. Корзина с id {} проверена, сформирован заказ. Вес: {}, объем: {}, хрупкость: {}.",
                shoppingCartId, order.getDeliveryWeight(), order.getDeliveryVolume(), order.getFragile());
        return order;
    }

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

    @Override
    public AddressDto getAddress() {
        log.info("Service. Обработка запроса на просмотр адреса склада.");
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .build();
    }

    private BookedProductsDto calculateBookingParameters(Map<UUID, Long> cartProducts,
                                                         Map<UUID, Product> warehouseProductsMap) {
        double deliveryWeight = 0;
        double deliveryVolume = 0;
        boolean fragile = false;
        List<UUID> lowQuantityProducts = new ArrayList<>();

        for (Map.Entry<UUID, Long> order : cartProducts.entrySet()) {

            UUID productId = order.getKey();
            Long orderedQuantity = order.getValue();

            Product product = warehouseProductsMap.get(productId);
            Long availableQuantity = product.getQuantity();

            if (availableQuantity < orderedQuantity) {
                log.debug("На складе не хватает товара с id: {}. Нужно {}, есть {}.",
                        productId, orderedQuantity, availableQuantity);
                lowQuantityProducts.add(productId);
            }

            deliveryWeight += product.getWeight() * orderedQuantity;
            deliveryVolume += product.getVolume() * orderedQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                fragile = true;
            }
        }

        if (!lowQuantityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("На складе не достаточно следующих товаров: "
                    + lowQuantityProducts);
        }

        return BookedProductsDto.builder()
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .fragile(fragile)
                .build();
    }

    private void throwIfExistsById(UUID productId) {
        if (repository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар с id: " + productId + " уже есть на складе.");
        }
    }

    private List<Product> findProductsOrElseThrow(Set<UUID> productIds) {
        List<Product> products = repository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            Set<UUID> foundIds = products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = new HashSet<>(productIds);
            missingIds.removeAll(foundIds);

            throw new NoSpecifiedProductInWarehouseException("На складе отсутствуют товары с id: " + missingIds);
        }

        return products;
    }

    private Product findProductOrElseThrow(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Товара с id: " + productId + " нет на складе."));
    }
}
