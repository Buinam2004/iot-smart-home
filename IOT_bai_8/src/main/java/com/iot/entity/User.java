package com.iot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name ="role", nullable = false)
    private String role = "USER";
    @PrePersist
    protected void onCreate() {
        if(role == null) {
            role = "USER";
        }
        createdAt = LocalDateTime.now();
    }
}
