package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "led_pir_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Led_Pir {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name ="device_id")
    private Integer deviceId;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private int state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

}