package ru.yandex.practicum.exceptions.handler;

public enum ErrorCodes {
    VALIDATION_FAILED("Ошибка валидации данных."),
    PRODUCT_IN_STORE_NOT_FOUND("Товар в магазине не найден."),
    INTERNAL_SERVER_ERROR("Внутренняя ошибка сервера."),
    NO_PRODUCT_IN_WAREHOUSE("Товар не зарегистрирован на складе."),
    PRODUCT_ALREADY_IN_WAREHOUSE("Товар уже зарегистрирован на складе."),
    LOW_QUANTITY_IN_WAREHOUSE("Недостаточно товара на складе."),
    NOT_AUTHORIZED_USER("Авторизация пользователя не пройдена."),
    SHOPPING_CART_NOT_FOUND("Корзина не найдена."),
    PRODUCT_IN_CART_NOT_FOUND("Товар в корзине не найден."),
    ORDER_BOOKING_ALREADY_EXIST("Товар на складе для заказа уже забронирован."),
    ORDER_BOOKING_NOT_FOUND("Бронь на складе для заказа не найдена."),
    PAYMENT_NOT_FOUND("Сведения об оплате не найдены."),
    DELIVERY_NOT_FOUND("Доставка не найдена."),
    ORDER_NOT_FOUND("заказ не найден."),
    ORDER_STATE_CONFLICT("Текущий статус заказа не позволяет выполнить операцию."),
    WAREHOUSE_SERVICE_UNAVAILABLE("Сервер склада недоступен."),
    SHOPPING_STORE_SERVICE_UNAVAILABLE("Сервер магазина недоступен."),
    ORDER_SERVICE_UNAVAILABLE("Сервер службы заказов недоступен."),
    DELIVERY_SERVICE_UNAVAILABLE("Сервер службы доставки недоступен."),
    PAYMENT_SERVICE_UNAVAILABLE("Сервер службы оплаты недоступен.");


    private final String message;

    ErrorCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
