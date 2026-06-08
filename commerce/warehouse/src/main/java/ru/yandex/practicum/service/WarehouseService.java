package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void addNewProduct(NewProductInWarehouseRequest newProductRequest);

    BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart);

    void addProductQuantity(AddProductToWarehouseRequest addProductRequest);

    AddressDto getAddress();

    BookedProductsDto assembleOrder(AssemblyProductsForOrderRequest assemblyRequest);

    void shipProductsToDelivery(ShippedToDeliveryRequest shippedRequest);

    void returnProducts(Map<UUID, Long> returnRequest);
}