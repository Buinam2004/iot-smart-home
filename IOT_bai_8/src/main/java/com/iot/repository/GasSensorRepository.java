package com.iot.repository;

import com.iot.entity.GasSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GasSensorRepository extends JpaRepository<GasSensor, Long> {
}
