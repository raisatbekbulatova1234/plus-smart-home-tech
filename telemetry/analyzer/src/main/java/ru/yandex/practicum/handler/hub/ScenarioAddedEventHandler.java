package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.service.ScenarioService;

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
