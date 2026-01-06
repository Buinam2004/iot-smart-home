package com.iot.repository;

import com.iot.entity.Rfid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfidRepository extends JpaRepository<Rfid, Integer> {
    boolean existsByUidAndDeviceId(String uid, Integer deviceId);
}
