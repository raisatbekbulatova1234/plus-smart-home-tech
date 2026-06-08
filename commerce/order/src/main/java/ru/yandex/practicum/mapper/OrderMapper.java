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

/**
 * MapStruct маппер для преобразования между DTO и Entity заказов и адресов
 * componentModel = "spring" - позволяет внедрять маппер через @Autowired
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ==================== АДРЕСА ====================

    /**
     * Преобразование AddressDto в Entity Address
     */
    @Mapping(target = "addressId", ignore = true)  // ID генерируется автоматически
    Address toAddress(AddressDto dto);

    /**
     * Преобразование Entity Address в AddressDto для ответа клиенту
     */
    AddressDto toAddressDto(Address address);

    // ==================== ЗАКАЗЫ ====================

    /**
     * Преобразование Entity Order в OrderDto для ответа клиенту
     * @param order Entity заказа из БД
     * @return DTO заказа
     */
    OrderDto toOrderDto(Order order);

    /**
     * Преобразование Entity Order в OrderContext (контекст для других сервисов)
     */
    OrderContext toOrderContext(Order order);

    // ==================== DTO ДЛЯ ДРУГИХ СЕРВИСОВ ====================

    /**
     * Преобразование OrderContext в NewDeliveryDto для сервиса доставки
     */
    NewDeliveryDto toNewDeliveryDto(OrderContext orderContext);

    /**
     * Преобразование OrderContext в OrderDtoDelivery для сервиса доставки
     */
    OrderDtoDelivery toOrderDtoDelivery(OrderContext orderContext);

    /**
     * Преобразование OrderContext в OrderDtoPayment для сервиса оплаты
     */
    OrderDtoPayment toOrderDtoPayment(OrderContext orderContext);

    /**
     * Частичное обновление существующего заказа из контекста
     * Обновляет только те поля, которые есть в контексте
     */
    @Mapping(target = "orderId", ignore = true)           // ID не меняем
    @Mapping(target = "shoppingCartId", ignore = true)    // ID корзины не меняем
    @Mapping(target = "username", ignore = true)          // Имя пользователя не меняем
    @Mapping(target = "toAddress", ignore = true)         // Адрес доставки не меняем
    @Mapping(target = "fromAddress", ignore = true)       // Адрес склада не меняем
    @Mapping(target = "products", ignore = true)          // Товары не меняем
    void updateOrderFromContext(
            OrderContext context,
            @MappingTarget Order order
    );

    // ==================== КАСТОМНЫЕ МЕТОДЫ ====================

    /**
     * Нормализация поля flat (номер квартиры)
     * Выполняется после маппинга адреса
     * Преобразует null в пустую строку
     */
    @AfterMapping
    default void normalizeFlat(@MappingTarget Address address) {
        if (address.getFlat() == null) {
            address.setFlat("");  // Защита от null в поле flat
        }
    }
}