package com.iot.repository;

import com.iot.entity.DhtSensor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhtRepository extends JpaRepository<DhtSensor, Integer> {
    Page<DhtSensor> findByDeviceIdOrderByReceivedAtDesc(Integer deviceId, Pageable pageable);
}
