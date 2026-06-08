package ru.yandex.practicum.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.model.Product;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

/**
 * MapStruct маппер для преобразования между DTO и Entity товаров
 * componentModel = "spring" - позволяет внедрять маппер через @Autowired
 */
@Mapper(componentModel = "spring")
public interface ShoppingStoreMapper {

    /**
     * Преобразование DTO для создания нового товара в Entity
     */
    @Mapping(target = "productId", ignore = true)  // Игнорируем ID - он генерируется автоматически
    Product toNewProduct(NewProductDto dto);

    /**
     * Частичное обновление существующего товара
     * Обновляет только не-null поля из DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)  // Не обновлять null поля
    void updateProduct(UpdatedProductDto dto, @MappingTarget Product product);

    /**
     * Преобразование Entity в DTO для отправки клиенту
     */
    ProductDto toProductDto(Product product);
}