package ru.yandex.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

/**
 * gRPC клиент для взаимодействия с HubRouter сервисом.
 *
 * HubRouter - это компонент системы, отвечающий за маршрутизацию команд
 * от анализатора к конкретным устройствам (хабам и датчикам).
 *
 * Назначение: отправка команд на выполнение действий (DeviceActionRequest)
 * через синхронный gRPC вызов.
 */
@Service
public class HubRouterClient {

    /**
     * gRPC клиент для синхронных (блокирующих) вызовов к HubRouter.
     *
     * Аннотация @GrpcClient внедряет сгенерированный gRPC-заглушку.
     * "hub-router" - имя клиента, настроенное в application.yml/application.properties.
     *
     * Конфигурация должна содержать:
     * grpc.client.hub-router.address=static://localhost:9090 (или другой адрес)
     */
    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub client;

    /**
     * Отправляет запрос на выполнение действия на устройстве.
     *
     * @param request - запрос, содержащий:
     *                 - hubId: идентификатор хаба-получателя
     *                 - scenarioName: название сценария (для логирования)
     *                 - timestamp: временная метка
     *                 - action: действие для выполнения (DeviceActionProto)
     *
     * Метод является синхронным (блокирующим) - ожидает ответ от сервера.
     * В случае ошибки выбрасывает gRPC-исключение.
     */
    public void send(DeviceActionRequest request) {

        client.handleDeviceAction(request);
    }
}