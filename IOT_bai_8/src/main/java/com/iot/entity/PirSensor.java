package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pir_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PirSensor extends BaseSensor {

    @Column(nullable = false)
    private int motion; // Comment  1 : motion detected, 0 : no motion

    @Column(nullable = false)
    private int light;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;
}
