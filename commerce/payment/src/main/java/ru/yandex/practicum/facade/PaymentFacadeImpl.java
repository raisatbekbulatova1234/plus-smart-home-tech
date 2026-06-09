package ru.yandex.practicum.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ProductPrice;
import ru.yandex.practicum.dto.order.OrderDtoPayment;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFacadeImpl implements PaymentFacade {
    private final PaymentService service;
    private final ShoppingStoreClientPaymentFacade shoppingStoreClient;

    @Override
    public PaymentDto createPayment(OrderDtoPayment request) {


        return service.createPayment(request);
    }

    @Override
    public BigDecimal calculateTotalPrice(OrderDtoPayment request) {
        return service.calculateTotalPrice(request);
    }

    @Override
    public BigDecimal calculateProductPrice(OrderDtoPayment request) {
        Map<UUID, Long> productDtoMap = request.getProducts();

        List<ProductPrice> itemsWithPrice = productDtoMap.entrySet().stream()
                .map(i -> {
                    BigDecimal price = shoppingStoreClient.getProductById(i.getKey()).getPrice();
                    return new ProductPrice(i.getKey(), i.getValue(), price);
                })
                .toList();

        return service.calculateProductPrice(itemsWithPrice);
    }

    @Override
    public void handleSuccessfulPayment(UUID paymentId) {
        service.handleSuccessfulPayment(paymentId);
    }

    @Override
    public void handleFailedPayment(UUID paymentId) {
        service.handleFailedPayment(paymentId);
    }

}
