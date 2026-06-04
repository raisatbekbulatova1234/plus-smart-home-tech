package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.service.ScenarioService;
/**
 * Обработчик события "Сценарий добавлен/обновлён" (ScenarioAddedEvent).
 *
 * Это событие приходит от хаба, когда пользователь создаёт или обновляет сценарий.
 *
 * Логика обработки:
 * - Сохраняет или обновляет сценарий в системе через ScenarioService
 * - Сценарий содержит: название, список условий, список действий
 */
@Component
public class ScenarioAddedEventHandler extends HubEventHandlerBase<ScenarioAddedEventAvro> {
    private final ScenarioService service;

    public ScenarioAddedEventHandler(ScenarioService service) {
        super(ScenarioAddedEventAvro.class);
        this.service = service;
    }

    @Override
    protected void process(HubEventAvro event,
                           ScenarioAddedEventAvro payload) {
        service.save(event.getHubId(), payload);
    }
}