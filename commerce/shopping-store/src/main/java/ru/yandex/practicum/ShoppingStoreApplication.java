package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Главный класс приложения Shopping Store (Магазин)
 */
@EnableFeignClients              // Включает поддержку декларативных HTTP клиентов Feign
// Позволяет вызывать другие микросервисы через интерфейсы
@SpringBootApplication           // Комбинирует:
// @Configuration - конфигурация Spring
// @EnableAutoConfiguration - авто-конфигурация Spring Boot
// @ComponentScan - сканирование компонентов в пакете
public class ShoppingStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingStoreApplication.class, args);
    }
}