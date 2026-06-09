package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartClient {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam String username);

    @PutMapping
    ShoppingCartDto addProducts(@RequestParam String username,
                                @RequestBody @NotEmpty Map<UUID, @Min(1) Long> products);

    @DeleteMapping
    void deactivateShoppingCart(@RequestParam String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProducts(@RequestParam String username,
                                   @RequestBody @NotEmpty List<UUID> products);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductsQuantity(@RequestParam String username,
                                           @RequestBody @Valid ChangeProductQuantityRequest request);
}
