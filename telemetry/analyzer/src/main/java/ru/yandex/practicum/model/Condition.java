package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

/**
 * Сущность "Условие" для базы данных.
 * Представляет собой критерий срабатывания сценария.
 * Пример: температура > 25 градусов
 */
@Entity
@Table(name = "conditions")
@Getter
@Setter
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionTypeAvro type; //тип условия

    @Enumerated(EnumType.STRING)
    private ConditionOperationAvro operation; //операция

    private Integer value; //значение
}