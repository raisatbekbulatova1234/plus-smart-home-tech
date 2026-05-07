package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.hubs.HubEvent;
import ru.yandex.practicum.dto.sensors.SensorEvent;
import ru.yandex.practicum.service.CollectorService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class CollectorController {
    private final CollectorService collectorService;

    @PostMapping("/sensors")
    public void prpcessSensorEvent(@Valid @RequestBody SensorEvent event) {
        collectorService.processSensorEvent(event);
    }

    @PostMapping("/hubs")
    public void processHubEvent(@Valid @RequestBody HubEvent event) {
        collectorService.processHubEvent(event);
    }
}
