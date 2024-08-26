package com.fridgeraid.fridgeraid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "ConsumptionRecords")
public class ConsumptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "price")
    private Integer price;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "consumption_date")
    private LocalDateTime consumptionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumption_type")
    private ConsumptionType consumptionType;

    @PrePersist
    protected void onCreate() {
        this.consumptionDate = LocalDateTime.now();
    }

}
