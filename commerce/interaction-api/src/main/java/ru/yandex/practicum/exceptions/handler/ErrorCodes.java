package ru.yandex.practicum.exceptions.handler;

public enum ErrorCodes {
    VALIDATION_FAILED("Ошибка валидации данных."),
    PRODUCT_NOT_FOUND("Товар не найден."),
    INTERNAL_SERVER_ERROR("Внутренняя ошибка сервера."),
    NO_PRODUCT_IN_WAREHOUSE("Товар не зарегистрирован на складе."),
    PRODUCT_ALREADY_IN_WAREHOUSE("Товар уже зарегистрирован на складе."),
    LOW_QUANTITY_IN_WAREHOUSE("Недостаточно товара на складе."),
    WAREHOUSE_SERVICE_UNAVAILABLE("Сервер склада недоступен."),
    NOT_AUTHORIZED_USER("Авторизация пользователя не пройдена."),
    SHOPPING_CART_NOT_FOUND("Корзина не найдена."),
    PRODUCT_IN_CART_NOT_FOUND("Товар в корзине не найден.");

    private final String message;

    ErrorCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
