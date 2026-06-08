package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.service.ScenarioService;

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
