package ru.yandex.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

@Service
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub client;

    public void send(DeviceActionRequest request) {
        client.handleDeviceAction(request);
    }
}
