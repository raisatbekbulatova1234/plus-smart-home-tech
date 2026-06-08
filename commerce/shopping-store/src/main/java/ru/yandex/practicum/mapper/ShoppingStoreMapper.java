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

@Mapper(componentModel = "spring")
public interface ShoppingStoreMapper {
    @Mapping(target = "productId", ignore = true)
    Product toNewProduct(NewProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateProduct(UpdatedProductDto dto, @MappingTarget Product product);

    ProductDto toProductDto(Product product);
}
