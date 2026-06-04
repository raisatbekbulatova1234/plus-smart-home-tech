package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Сущность "Сценарий" для базы данных.
 * Представляет собой правило "Если условие, то действие".
 * Пример: Если температура > 25°, то включить кондиционер
 */
@Entity
@Table(name = "scenarios")
@Getter
@Setter
public class Scenario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;                    // ID хаба, к которому привязан сценарий

    private String name;                     // имя сценария

    /**
     * Карта условий сценария.
     * Ключ: sensor_id - ID датчика
     * Значение: Condition - условие проверки
     * Пример: "temp_sensor_1" -> температура > 25°
     */
    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL)    // Каскадные операции: сохранение/удаление условий вместе со сценарием

    @MapKeyColumn(table = "scenario_conditions",  // Колонка-ключ в промежуточной таблице
            name = "sensor_id")             // Имя колонки для ключа (ID датчика)

    @JoinTable(
            name = "scenario_conditions",
            joinColumns = @JoinColumn(name = "scenario_id"),     // Внешний ключ на Scenario
            inverseJoinColumns = @JoinColumn(name = "condition_id") // Внешний ключ на Condition
    )
    private Map<String, Condition> conditions = new HashMap<>();  // Условия, привязанные к датчикам


    /**
     * Карта действий сценария.
     * Ключ: sensor_id - ID устройства-исполнителя
     * Значение: Action - действие для выполнения
     * Пример: "ac_sensor_1" -> включить, установить 22°
     */
    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL)

    @MapKeyColumn(table = "scenario_actions",
            name = "sensor_id")

    @JoinTable(
            name = "scenario_actions",
            joinColumns = @JoinColumn(name = "scenario_id"),   // Внешний ключ на Scenario
            inverseJoinColumns = @JoinColumn(name = "action_id") // Внешний ключ на Action
    )
    private Map<String, Action> actions = new HashMap<>();  // Действия, привязанные к устройствам
}