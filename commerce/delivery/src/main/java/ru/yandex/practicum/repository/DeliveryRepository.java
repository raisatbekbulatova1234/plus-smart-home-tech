package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Delivery;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Delivery (доставка)
 * Наследует стандартные методы CRUD от JpaRepository
 */
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    /**
     * Поиск доставки по ID с жадной загрузкой адресов
     *
     * Использует LEFT JOIN FETCH для предотвращения LazyInitializationException
     * Загружает fromAddress и toAddress одним запросом
     */
    @Query("SELECT d FROM Delivery d " +
            "LEFT JOIN FETCH d.fromAddress " +   // Жадная загрузка адреса отправления
            "LEFT JOIN FETCH d.toAddress " +     // Жадная загрузка адреса доставки
            "WHERE d.deliveryId = :deliveryId")
    Optional<Delivery> findByIdWithAddresses(@Param("deliveryId") UUID deliveryId);
}