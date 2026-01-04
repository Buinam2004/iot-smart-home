package com.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.iot.repository")
@EntityScan(basePackages = "com.iot.entity")
public class IotManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(IotManagementApplication.class, args);
    }
}
