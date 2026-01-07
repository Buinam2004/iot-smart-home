package com.iot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "gas_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasSensor extends BaseSensor {

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false)
    private int state;
}
