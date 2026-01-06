package com.iot.service;

import com.iot.custom.CustomUserDetails;
import com.iot.dto.CreateDeviceDTO;
import com.iot.dto.DeviceDTO;
import com.iot.dto.UpdateDeviceDTO;
import com.iot.entity.Device;
import com.iot.repository.DeviceRepository;
import com.iot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceService implements IDeviceService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private UserRepository userRepository;

    public List<DeviceDTO> getAllDevices() {
        List<Device> devices = deviceRepository.findAllWithUser();
        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<DeviceDTO> getDeviceById(Integer id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        System.out.println(device);
        return Optional.ofNullable(convertToDTO(device));

    }
    
    public Device createDevice(Integer userId, CreateDeviceDTO device) {
        Device newDevice = new Device();
        newDevice.setMacAddress(device.getMacAddress());
        newDevice.setName(device.getName());
        newDevice.setUserId(userId);

        return deviceRepository.save(newDevice);
    }
    
    public UpdateDeviceDTO updateDevice(Integer id, Device deviceDetails) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

        UpdateDeviceDTO updateDeviceDTO = new UpdateDeviceDTO();
        device.setId(id);
        device.setMacAddress(deviceDetails.getMacAddress());
        device.setName(deviceDetails.getName());
        device.setUserId(deviceDetails.getUserId());
        device.setIsOnline(deviceDetails.getIsOnline());
        deviceRepository.save(device);
        return  updateDeviceDTO.builder()
                .deviceKey(deviceDetails.getMacAddress())
                .name(deviceDetails.getName())
                .userId(deviceDetails.getUserId())
                .isOnline(deviceDetails.getIsOnline())
                .id(device.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public void deleteDevice(Integer id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        deviceRepository.delete(device);
    }
    


    @Override
    public List<DeviceDTO> findByCreatorUsernameAndStatus(String username, String status) {
        List<Device> devices = deviceRepository.findByCreatorUsername(username);
        if (status.equalsIgnoreCase("online")) {
            List<Device> filteredDevices = devices.stream()
                    .filter(Device::getIsOnline)
                    .collect(Collectors.toList());
            return filteredDevices.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            List<Device> filteredDevices = devices.stream()
                    .filter(device -> !device.getIsOnline())
                    .collect(Collectors.toList());
            return filteredDevices.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    public boolean isOwner(Authentication authentication, Integer deviceId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        log.info("Authentication object: {}", authentication);
        Object principal = authentication.getPrincipal();

        // Kiểm tra xem principal có phải là CustomUserDetails không
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails currentUser = (CustomUserDetails) principal;
        log.info("Current user: {}", currentUser);
        Integer currentUserId = currentUser.getUserId(); // Lấy ID từ CustomUserDetails bạn đã tạo

        log.info("Checking ownership for UserID: {} and DeviceID: {}", currentUserId, deviceId);

        // Tìm thiết bị và kiểm tra xem userId của chủ sở hữu có khớp không
        boolean check = deviceRepository.existsByIdAndUserId(deviceId, currentUserId);
        log.info("Check ownership result: {}", check);
        return check;
    }

    // Chuyển đổi Entity sang DTO
    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setUserId(device.getUserId());
        dto.setName(device.getName());
        dto.setMacAddress(device.getMacAddress());
        dto.setIsOnline(device.getIsOnline());
        dto.setCreatedAt(device.getCreatedAt());
        
        if (device.getUser() != null) {
            dto.setCreatorName(device.getUser().getUsername());
        }
        
        return dto;
    }
}
