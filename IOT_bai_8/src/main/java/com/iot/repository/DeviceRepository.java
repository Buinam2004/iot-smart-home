package com.iot.repository;

import com.iot.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    @Query("SELECT d FROM Device d JOIN d.user u WHERE u.username = :username")
    List<Device> findByCreatorUsername(@Param("username") String username);

    @Query("SELECT d FROM Device d JOIN FETCH d.user")
    List<Device> findAllWithUser();

    List<Device> findAllByUserId(int userId);

    boolean existsByIdAndUserId(Integer id, Integer userId);

    boolean existsByIdAndMacAddress( Integer id, String macAddress);

    Device findByMacAddress(String macAddress);

}
