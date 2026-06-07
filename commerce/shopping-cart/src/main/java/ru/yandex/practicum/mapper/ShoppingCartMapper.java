package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

/**
 * MapStruct маппер для преобразования между Entity корзины и DTO
 * componentModel = "spring" - позволяет внедрять маппер через @Autowired
 */
@Mapper(componentModel = "spring")
public interface ShoppingCartMapper {

    ShoppingCartDto toShoppingCartDto(ShoppingCart shoppingCart);
}