package ru.yandex.practicum.exceptions.cart;

public class NoProductsInShoppingCartException extends RuntimeException {
  public NoProductsInShoppingCartException(String message) {
    super(message);
  }
}
