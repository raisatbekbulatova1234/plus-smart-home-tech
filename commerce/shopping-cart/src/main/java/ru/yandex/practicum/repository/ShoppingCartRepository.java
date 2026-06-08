package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.enums.ShoppingCartState;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    @EntityGraph(attributePaths = "products")
    Optional<ShoppingCart> findByUsername(String username);

    @EntityGraph(attributePaths = "products")
    Optional<ShoppingCart> findByUsernameAndState(String username, ShoppingCartState state);
}
