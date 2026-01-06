package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "dht_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhtSensor extends BaseSensor {

    @Column(nullable = false)
    private Double temperature = 0.0;

    @Column(nullable = false)
    private Double humidity = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;


}
