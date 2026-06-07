package ru.yandex.practicum.exceptions.warehouse;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(message);
    }
}
