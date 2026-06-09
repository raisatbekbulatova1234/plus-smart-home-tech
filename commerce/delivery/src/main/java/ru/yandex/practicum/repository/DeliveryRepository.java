package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Delivery;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    @Query("SELECT d FROM Delivery d " +
            "LEFT JOIN FETCH d.fromAddress " +
            "LEFT JOIN FETCH d.toAddress " +
            "WHERE d.deliveryId = :deliveryId")
    Optional<Delivery> findByIdWithAddresses(@Param("deliveryId") UUID deliveryId);
}
