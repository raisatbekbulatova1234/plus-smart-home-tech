package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.enums.ShoppingCartState;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью ShoppingCart (корзина покупок)
 */
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    /**
     * Поиск корзины по имени пользователя с жадной загрузкой товаров
     *
     * @EntityGraph(attributePaths = "products") - предотвращает LazyInitializationException
     * Загружает коллекцию products сразу одним запросом (JOIN FETCH)
     */
    @EntityGraph(attributePaths = "products")
    Optional<ShoppingCart> findByUsername(String username);

    /**
     * Поиск корзины по имени пользователя и состоянию с жадной загрузкой товаров
     */
    @EntityGraph(attributePaths = "products")
    Optional<ShoppingCart> findByUsernameAndState(String username, ShoppingCartState state);
}