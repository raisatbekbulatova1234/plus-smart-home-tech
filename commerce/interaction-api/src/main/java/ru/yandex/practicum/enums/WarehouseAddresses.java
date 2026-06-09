package ru.yandex.practicum.enums;

public enum WarehouseAddresses {

    ADDRESS_1("ADDRESS_1"),
    ADDRESS_2("ADDRESS_2");

    private final String street;

    WarehouseAddresses(String street) {
        this.street = street;
    }

    public String getStreet() {
        return street;
    }
}
