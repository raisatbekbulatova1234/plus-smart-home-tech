package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность адреса (доставки/отправления)
 * Маппится на таблицу addresses в БД
 * Используется для хранения адресов: клиентов, складов и т.д.
 */
@Entity
@Table(name = "addresses", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID addressId;

    @Column(name = "country", nullable = false)  // NOT NULL
    private String country;             // Страна

    @Column(name = "city", nullable = false)     // NOT NULL
    private String city;                // Город

    @Column(name = "street", nullable = false)   // NOT NULL
    private String street;              // Улица

    @Column(name = "house", nullable = false)    // NOT NULL
    private String house;               // Номер дома

    @Column(name = "flat")
    private String flat;                // Номер квартиры (опционально)
}