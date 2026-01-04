package com.iot.service;

import com.iot.dto.CreateDeviceDTO;
import com.iot.dto.DeviceDTO;
import com.iot.dto.UpdateDeviceDTO;
import com.iot.entity.Device;

import java.util.List;
import java.util.Optional;

public interface IDeviceService {
    List<DeviceDTO> getAllDevices();
    Optional<DeviceDTO> getDeviceById(Integer id);
    Device createDevice(Integer userId, CreateDeviceDTO device);
    UpdateDeviceDTO updateDevice(Integer id, Device deviceDetails);
    void deleteDevice(Integer id);
    List<DeviceDTO> findByCreatorUsernameAndStatus(String creatornName, String status);


}
