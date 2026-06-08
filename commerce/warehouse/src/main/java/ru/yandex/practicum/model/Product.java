package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @Column(name = "depth")
    private Double depth;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "quantity")
    @Builder.Default
    private Long quantity = 0L;

    public double getVolume() {
        if (width == null || height == null || depth == null) {
            return 0;
        }

        return width * height * depth;
    }
}
