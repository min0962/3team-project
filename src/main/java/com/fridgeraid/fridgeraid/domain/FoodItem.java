package com.fridgeraid.fridgeraid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "FoodItems")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "price")
    private Integer price = 0;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_method")
    private StorageMethod storageMethod;

    @PrePersist
    protected void onCreate() {
        this.registeredDate = LocalDateTime.now();
    }
}
