package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.store.NewProductDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityState;
import ru.yandex.practicum.dto.store.UpdatedProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.mapper.ShoppingStoreMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;
import ru.yandex.practicum.exceptions.store.ProductNotFoundException;

import java.util.UUID;

/**
 * Реализация сервиса для управления товарами в магазине
 * Содержит бизнес-логику работы с продуктами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ShoppingStoreRepository repository;
    private final ShoppingStoreMapper mapper;

    /**
     * Получение товаров по категории с пагинацией
     * Транзакция только для чтения (оптимизация производительности)
     */
    @Override
    @Transactional(readOnly = true)  // Только чтение, нет изменений в БД
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        log.debug("Service. Обработка запроса на просмотр товаров категории {} с пагинацией {}.",
                category, pageable);

        Page<Product> searchResult = repository.findByProductCategory(category, pageable);

        log.info("Service. Запрос на просмотр товаров категории {} обработан. " +
                "Количество найденных записей: {}", category, searchResult.getContent().size());

        // Преобразование Page<Product> в Page<ProductDto> с помощью маппера
        return searchResult.map(mapper::toProductDto);
    }

    /**
     * Добавление нового товара
     * Транзакция с записью в БД
     */
    @Override
    @Transactional  // Выполняется в транзакции (при ошибке будет откат)
    public ProductDto addProduct(NewProductDto newProduct) {
        log.debug("Service. Обработка запроса на добавление нового товара {}.", newProduct.getProductName());

        Product product = mapper.toNewProduct(newProduct);

        Product savedProduct = repository.save(product);

        log.info("Service. Запрос на добавление нового товара обработан, присвоен id: {}",
                savedProduct.getProductId());

        // Преобразование обратно в DTO для ответа клиенту
        return mapper.toProductDto(savedProduct);
    }

    /**
     * Обновление существующего товара
     * Поддерживает частичное обновление (только переданные поля)
     */
    @Override
    @Transactional
    public ProductDto updateProduct(UpdatedProductDto updatedProduct) {
        UUID productId = updatedProduct.getProductId();
        log.debug("Service. Обработка запроса на обновление товара с id: {}.", productId);

        Product existingProduct = findProductOrElseThrow(productId);

        // Частичное обновление полей (null поля не обновляются)
        mapper.updateProduct(updatedProduct, existingProduct);

        // JPA автоматически сохранит изменения при коммите транзакции
        log.info("Service. Запрос на обновление товара c id {} обработан.", productId);

        return mapper.toProductDto(existingProduct);
    }

    /**
     * Деактивация товара (мягкое удаление)
     * Товар не удаляется из БД, а меняет статус на DEACTIVATE
     */
    @Override
    @Transactional
    public boolean deactivateProduct(UUID productId) {
        log.debug("Service. Обработка запроса на деактивацию товара с id {}.", productId);

        Product existingProduct = findProductOrElseThrow(productId);

        if (existingProduct.getProductState() == ProductState.DEACTIVATE) {
            log.info("Товар с id {} уже деактивирован.", productId);
            return false;
        }

        existingProduct.setProductState(ProductState.DEACTIVATE);
        log.info("Service. Запрос на деактивацию товара c id {} обработан.", productId);
        return true;
    }

    /**
     * Изменение состояния количества товара
     * Используется для отслеживания остатков (ENDED / SUFFICIENT)
     */
    @Override
    @Transactional
    public boolean setProductQuantityState(SetProductQuantityState quantityState) {
        UUID productId = quantityState.getProductId();
        QuantityState newQuantityState = quantityState.getQuantityState();

        log.debug("Service. Обработка запроса на установку количества товара с id {}, количество товара: {}.",
                productId, newQuantityState);

        Product existingProduct = findProductOrElseThrow(productId);

        if (existingProduct.getQuantityState() == newQuantityState) {
            log.info("Товар с id {} уже имеет QuantityState {}.", productId, newQuantityState);
            return false;
        }

        existingProduct.setQuantityState(newQuantityState);
        log.info("Service. Запрос на установку количества товара с id {} обработан.", productId);
        return true;
    }

    /**
     * Получение товара по ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        log.debug("Service. Обработка запроса на просмотр товара с id {}.", productId);

        Product product = findProductOrElseThrow(productId);

        log.info("Service. Запрос на просмотр товара с id {} обработан.", productId);

        return mapper.toProductDto(product);
    }

    /**
     * Вспомогательный метод для поиска товара по ID
     * Если товар не найден - выбрасывает ProductNotFoundException
     */
    private Product findProductOrElseThrow(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар с id: " + productId + " не найден."));
    }
}