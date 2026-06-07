package ru.yandex.practicum.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация фасада корзины покупок
 *
 * Объединяет:
 * - Бизнес-логику корзины (через ShoppingCartService)
 * - Проверку склада (через WarehouseClientFacade)
 * - Маппинг Entity <-> DTO
 */
@Service
@RequiredArgsConstructor
public class ShoppingCartFacadeImpl implements ShoppingCartFacade {

    private final ShoppingCartService service;           // Сервис для работы с корзиной
    private final WarehouseClientFacade warehouseClient; // Фасад для проверки склада
    private final ShoppingCartMapper mapper;             // Маппер для преобразований

    /**
     * Получение корзины пользователя
     * Без проверки склада (просто возвращаем текущее состояние)
     */
    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart shoppingCart = service.getShoppingCart(username);
        return mapper.toShoppingCartDto(shoppingCart);
    }

    /**
     * Деактивация корзины пользователя
     */
    @Override
    public void deactivateShoppingCart(String username) {
        service.deactivateShoppingCart(username);
    }

    /**
     * Удаление товаров из корзины
     * Без проверки склада (удаление всегда возможно)
     */
    @Override
    public ShoppingCartDto removeProducts(String username, List<UUID> products) {
        ShoppingCart shoppingCart = service.removeProducts(username, products);
        return mapper.toShoppingCartDto(shoppingCart);
    }

    /**
     * Добавление товаров в корзину
     */
    @Override
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        // 1. Получаем актуальную корзину из БД
        ShoppingCart cart = service.getShoppingCart(username);

        // 2. Создаем DTO для проверки на складе (не сохраняем в БД)
        ShoppingCartDto draft = mapper.toShoppingCartDto(cart);
        Map<UUID, Long> productsInDraft = draft.getProducts();

        // 3. Временно добавляем новые товары в DTO для проверки
        products.forEach((productId, quantity) ->
                productsInDraft.merge(productId, quantity, Long::sum)  // Суммируем количества
        );

        // 4. Проверяем доступность товаров на складе
        warehouseClient.checkShoppingCart(draft);

        // 5. Сохраняем изменения в БД (только после успешной проверки)
        cart = service.addProducts(username, products);
        return mapper.toShoppingCartDto(cart);
    }

    /**
     * Изменение количества товара в корзине
     */
    @Override
    public ShoppingCartDto changeProductsQuantity(String username, ChangeProductQuantityRequest request) {
        // 1. Получаем активную корзину
        ShoppingCart cart = service.getActiveShoppingCart(username);

        UUID productId = request.getProductId();

        // 2. Проверяем, что товар есть в корзине
        service.validateProductExists(productId, cart);

        // 3. Создаем DTO для проверки на складе
        ShoppingCartDto draft = mapper.toShoppingCartDto(cart);

        // 4. Обновляем количество в DTO (временное состояние)
        draft.getProducts().put(productId, request.getNewQuantity());

        // 5. Проверяем доступность товара на складе
        warehouseClient.checkShoppingCart(draft);

        // 6. Сохраняем изменения в БД
        cart = service.changeProductsQuantity(username, request);
        return mapper.toShoppingCartDto(cart);
    }
}