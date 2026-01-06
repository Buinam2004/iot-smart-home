package com.iot.repository;

import com.iot.entity.Led_Pir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Led_PirRepository extends JpaRepository<Led_Pir, Integer> {
}
