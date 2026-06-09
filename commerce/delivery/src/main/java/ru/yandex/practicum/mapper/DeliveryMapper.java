package ru.yandex.practicum.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.NewDeliveryDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    @Mapping(target = "addressId", ignore = true)
    Address toAddress(AddressDto dto);

    AddressDto toAddressDto(Address address);

    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "deliveryState", ignore = true)
    Delivery toDelivery(NewDeliveryDto dto);

    DeliveryDto toDeliveryDto(Delivery delivery);

    @AfterMapping
    default void normalizeFlat(@MappingTarget Address address) {
        if (address.getFlat() == null) {
            address.setFlat("");
        }
    }
}
