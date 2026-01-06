package com.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "door_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Door {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    private String event;

    @Column(name ="device_id")
    private Integer deviceId;

    @Column(name = "receive_at", nullable = false)
    private LocalDateTime receiveAt;

    private String type;

    private String action; // Check ? Deny ? Open
}