package com.iot.repository;

import com.iot.entity.DhtSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhtSensorRepository extends JpaRepository<DhtSensor, Integer> {
}
