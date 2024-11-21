package com.nvs.store.models.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    @NonNull
    private Long id;
    @NonNull
    private String title;
    @NonNull
    private Integer available;
    @NonNull
    private BigDecimal price;

}
