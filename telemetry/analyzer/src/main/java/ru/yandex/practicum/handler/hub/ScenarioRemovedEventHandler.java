package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.service.ScenarioService;
/**
 * Обработчик события "Сценарий удалён" (ScenarioRemovedEvent).
 *
 * Это событие приходит от хаба, когда пользователь удаляет сценарий.
 *
 * Логика обработки:
 * - Удаляет сценарий из системы через ScenarioService
 * - Каскадно удаляются все связанные условия и действия
 */
@Component
public class ScenarioRemovedEventHandler extends HubEventHandlerBase<ScenarioRemovedEventAvro> {
    private final ScenarioService service;

    public ScenarioRemovedEventHandler(ScenarioService service) {
        super(ScenarioRemovedEventAvro.class);
        this.service = service;
    }

    @Override
    protected void process(HubEventAvro event,
                           ScenarioRemovedEventAvro payload) {
        service.delete(event.getHubId(), payload.getName());
    }
}