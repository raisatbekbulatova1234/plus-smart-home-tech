package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
/**
 * Сущность "Условие сценария" для базы данных.
 * Представляет собой связь между сценарием, датчиком и условием.
 * Является промежуточной таблицей для связи Many-to-Many между Scenario и Condition.
 */
@Entity
@Table(name = "scenario_conditions")
@Getter
@Setter
public class ScenarioCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связь "многие условия к одному сценарию". Один сценарий может иметь много условий.
     //optional = false означает, что поле не может быть null.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    //Связь "многие условия к одному датчику". Один датчик может участвовать в многих условиях.
    //optional = false означает, что поле не может быть null.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    //Связь "многие связи к одному условию". Одно условие может использоваться в разных сценариях.
    //optional = false означает, что поле не может быть null.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condition_id")
    private Condition condition;
}