package ru.yandex.practicum.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.order.OrderContext;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderDtoDelivery;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "addressId", ignore = true)
    Address toAddress(AddressDto dto);

    AddressDto toAddressDto(Address address);

    OrderDto toOrderDto(Order order);

    OrderContext toOrderContext(Order order);

    NewDeliveryDto toNewDeliveryDto(OrderContext orderContext);

    OrderDtoDelivery toOrderDtoDelivery(OrderContext orderContext);

    OrderDtoPayment toOrderDtoPayment(OrderContext orderContext);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "shoppingCartId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "toAddress", ignore = true)
    @Mapping(target = "fromAddress", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateOrderFromContext(
            OrderContext context,
            @MappingTarget Order order
    );

    @AfterMapping
    default void normalizeFlat(@MappingTarget Address address) {
        if (address.getFlat() == null) {
            address.setFlat("");
        }
    }
}
