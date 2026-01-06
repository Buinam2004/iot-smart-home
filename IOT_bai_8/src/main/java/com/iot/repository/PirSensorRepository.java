package com.iot.repository;

import com.iot.entity.PirSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PirSensorRepository extends JpaRepository<PirSensor, Integer> {
}
