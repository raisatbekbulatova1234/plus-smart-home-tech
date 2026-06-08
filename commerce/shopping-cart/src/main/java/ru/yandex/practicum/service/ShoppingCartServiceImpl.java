package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.enums.ShoppingCartState;
import ru.yandex.practicum.exceptions.cart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exceptions.cart.NotAuthorizedUserException;
import ru.yandex.practicum.exceptions.cart.ShoppingCartNotFoundException;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

/**
 * Реализация сервиса для работы с корзиной покупок
 * Содержит бизнес-логику управления корзиной
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository repository;

    /**
     * Получение корзины пользователя
     * Если корзина не существует - создается новая
     * Транзакция с записью (может создавать новую корзину)
     */
    @Override
    @Transactional
    public ShoppingCart getShoppingCart(String username) {
        log.debug("Service. От пользователя {} поступил запрос на создание " +
                "или просмотр существующей корзины.", username);
        validateUsername(username);
        return getOrCreateShoppingCart(username);
    }

    /**
     * Получение активной корзины пользователя (только для чтения)
     * Если корзина не найдена - выбрасывает исключение
     */
    @Override
    @Transactional(readOnly = true)
    public ShoppingCart getActiveShoppingCart(String username) {
        log.debug("Service. От пользователя {} поступил запрос на просмотр активной корзины.", username);
        validateUsername(username);
        return findActiveShoppingCartOrThrow(username);
    }

    /**
     * Деактивация корзины пользователя (мягкое удаление)
     * Меняет статус корзины с ACTIVE на DEACTIVATE
     */
    @Override
    @Transactional
    public void deactivateShoppingCart(String username) {
        log.debug("Service. От пользователя {} поступил запрос на деактивацию корзины.", username);
        validateUsername(username);

        ShoppingCart shoppingCart = findActiveShoppingCartOrThrow(username);
        shoppingCart.setState(ShoppingCartState.DEACTIVATE);

        log.info("Service. Корзина пользователя {} с id {} деактивирована.",
                username, shoppingCart.getShoppingCartId());
    }

    /**
     * Удаление конкретных товаров из корзины
     * Проверяет, что все указанные товары присутствуют в корзине
     */
    @Override
    @Transactional
    public ShoppingCart removeProducts(String username, List<UUID> products) {
        log.debug("Service. От пользователя {} поступил запрос " +
                "на удаление товаров из корзины: {}.", username, products);
        validateUsername(username);

        ShoppingCart shoppingCart = findActiveShoppingCartOrThrow(username);
        Map<UUID, Long> productsInCart = shoppingCart.getProducts();

        // Поиск товаров, которых нет в корзине
        List<UUID> missingProducts = products.stream()
                .filter(id -> !productsInCart.containsKey(id))
                .toList();

        // Если есть отсутствующие товары - выбрасываем исключение
        if (!missingProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException("В корзине пользователя: " + username +
                    " нет товаров с id: " + missingProducts);
        }

        // Удаляем все указанные товары
        products.forEach(productsInCart::remove);

        log.info("Service. Из корзины пользователя {} удалены товары: {}.", username, products);
        return shoppingCart;
    }

    /**
     * Добавление товаров в корзину
     * Если товар уже есть - количество суммируется
     */
    @Override
    @Transactional
    public ShoppingCart addProducts(String username, Map<UUID, Long> products) {
        log.debug("Service. От пользователя {} поступил запрос на " +
                "добавление {} товаров в корзину.", username, products.size());

        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);
        Map<UUID, Long> cartProducts = shoppingCart.getProducts();

        // Суммируем количества для каждого товара
        products.forEach((productId, quantity) ->
                cartProducts.merge(productId, quantity, Long::sum)
        );

        log.info("Service. Запрос от пользователя {} о добавлении {} товаров в корзину обработан.",
                username, products.size());
        return shoppingCart;
    }

    /**
     * Изменение количества конкретного товара в корзине
     */
    @Override
    @Transactional
    public ShoppingCart changeProductsQuantity(String username, ChangeProductQuantityRequest request) {
        log.debug("Service. От пользователя {} поступил запрос " +
                "на изменение количества товара с id {} в корзине.", username, request.getProductId());

        ShoppingCart shoppingCart = findActiveShoppingCartOrThrow(username);
        UUID productId = request.getProductId();
        Map<UUID, Long> productsInCart = shoppingCart.getProducts();

        // Сохраняем старое количество для логирования
        Long oldQuantity = productsInCart.replace(productId, request.getNewQuantity());

        log.info("Service. В корзине пользователя {} количество товара с id {} изменено: {} -> {}.",
                username,
                productId,
                oldQuantity,
                productsInCart.get(productId)
        );

        return shoppingCart;
    }

    /**
     * Проверка существования товара в корзине
     */
    @Override
    public void validateProductExists(UUID productId, ShoppingCart shoppingCart) {
        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException("В корзине нет товара с id: " + productId);
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Получение корзины пользователя или создание новой
     */
    private ShoppingCart getOrCreateShoppingCart(String username) {
        Optional<ShoppingCart> shoppingCartOpt = repository.findByUsername(username);

        if (shoppingCartOpt.isEmpty()) {
            return createShoppingCart(username);
        }

        ShoppingCart shoppingCart = shoppingCartOpt.get();
        log.info("Service. Корзина с id {} найдена.", shoppingCart.getShoppingCartId());
        return shoppingCart;
    }

    /**
     * Создание новой корзины для пользователя
     */
    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .username(username)
                .build();  // state по умолчанию ACTIVE (из @Builder.Default)

        shoppingCart = repository.save(shoppingCart);
        log.info("Service. Для пользователя {} создана новая корзина, присвоен id: {}.",
                username, shoppingCart.getShoppingCartId());
        return shoppingCart;
    }

    /**
     * Валидация имени пользователя
     */
    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
    }

    /**
     * Поиск активной корзины или выбрасывание исключения
     */
    private ShoppingCart findActiveShoppingCartOrThrow(String username) {
        return repository.findByUsernameAndState(username, ShoppingCartState.ACTIVE)
                .orElseThrow(() -> new ShoppingCartNotFoundException("Активная корзина пользователя: " + username +
                        " не найдена."));
    }
}