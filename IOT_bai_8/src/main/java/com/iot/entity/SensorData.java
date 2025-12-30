package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "device_id", nullable = false)
    private Integer deviceId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private Double value = 0.0;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
        if (value == null) {
            value = 0.0;
        }
    }
}
