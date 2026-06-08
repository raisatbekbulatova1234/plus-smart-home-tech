package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"toAddress", "products"})
    Page<Order> findByUsername(String username, Pageable pageable);

    @EntityGraph(attributePaths = {"toAddress", "products"})
    Optional<Order> findByOrderId(UUID orderId);
}