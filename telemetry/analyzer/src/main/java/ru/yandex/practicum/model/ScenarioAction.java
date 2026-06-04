package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность "Действие сценария" для базы данных.
 * Представляет собой связь между сценарием, датчиком и действием.
 * Является промежуточной таблицей для связи Many-to-Many между Scenario и Action.
 */
@Entity
@Table(name = "scenario_actions")
@Getter
@Setter
public class ScenarioAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связь "многие действия к одному сценарию". Один сценарий может иметь много действий
    @ManyToOne(fetch = FetchType.LAZY)// Ленивая загрузка (загружается только при обращении)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    //Связь "многие действия к одному датчику". Один датчик может быть исполнителем многих действий.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    //Связь "многие связи к одному действию". Одно действие может использоваться в разных сценариях.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;
}