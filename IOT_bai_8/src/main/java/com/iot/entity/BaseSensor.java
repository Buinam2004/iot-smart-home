package com.iot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@MappedSuperclass // Quan trọng: Để JPA biết đây là class chứa các cột dùng chung
@Getter
@Setter
public class BaseSensor {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "device_id", nullable = false)
        private Integer deviceId;

        @Column(nullable = false)
        private String type;

        private String sensor;

        private LocalDateTime receivedAt;
}
