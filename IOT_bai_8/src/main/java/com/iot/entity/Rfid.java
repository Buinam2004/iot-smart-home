package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfid_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rfid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    @Column(name ="device_id")
    private Integer deviceId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
