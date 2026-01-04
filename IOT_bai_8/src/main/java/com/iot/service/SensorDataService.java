package com.iot.service;

import com.iot.custom.CustomUserDetails;
import com.iot.dto.SensorDataDTO;
import com.iot.dto.UpdateSensorDataDTO;
import com.iot.entity.Device;
import com.iot.entity.SensorData;
import com.iot.exception.ResourceNotFoundException;
import com.iot.repository.DeviceRepository;
import com.iot.repository.SensorDataRepository;
import com.iot.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SensorDataService implements ISensorDataService {
    
    // Use constructor injection instead of field injection for better testability
    private final SensorDataRepository sensorDataRepository;
    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;

    public SensorDataService(SensorDataRepository sensorDataRepository,
                             DeviceService deviceService,
                             DeviceRepository deviceRepository) {
        this.sensorDataRepository = sensorDataRepository;
        this.deviceService = deviceService;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<SensorDataDTO> getAllSensorData() {
        List<SensorData> sensorDataList = sensorDataRepository.findAllWithDevice();
        return sensorDataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<SensorDataDTO> getSensorDataById(Integer id) {
        SensorData sensorData = sensorDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor Data", "id", id));
        System.out.println(sensorData);
        return Optional.ofNullable(convertToDTO(sensorData));
    }
    
    @Override
    public SensorData createSensorData(SensorData sensorData) {
        if (sensorData == null) {
            throw new IllegalArgumentException("sensorData cannot be null");
        }
        // ensure a receivedAt timestamp exists
        if (sensorData.getReceivedAt() == null) {
            sensorData.setReceivedAt(LocalDateTime.now());
        }
        return sensorDataRepository.save(sensorData);
    }
    
    @Override
    public UpdateSensorDataDTO updateSensorData(Integer id, SensorData sensorDataDetails) {
        if (sensorDataDetails == null) {
            throw new IllegalArgumentException("sensorDataDetails cannot be null");
        }
        SensorData sensorData = sensorDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor Data", "id", id));

        UpdateSensorDataDTO updateSensorDataDTO = new UpdateSensorDataDTO();
        sensorData.setDeviceId(sensorDataDetails.getDeviceId());
        sensorData.setType(sensorDataDetails.getType());
        sensorData.setValue(sensorDataDetails.getValue());
        sensorDataRepository.save(sensorData);
        return updateSensorDataDTO.builder()
                .deviceId(sensorDataDetails.getDeviceId())
                .type(sensorDataDetails.getType())
                .value(sensorDataDetails.getValue())
                .receivedAt(LocalDateTime.now())
                .id(sensorData.getId())
                .build();
    }
    
    @Override
    public void deleteSensorData(Integer id) {
        SensorData sensorData = sensorDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor Data", "id", id));
        sensorDataRepository.delete(sensorData);
    }
    
    @Override
    public List<SensorDataDTO> findByDeviceName(String deviceName) {
        List<SensorData> sensorDataList = sensorDataRepository.findByDeviceName(deviceName);
        return sensorDataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<SensorDataDTO> getAllRecentSensorDataWithDeviceName(String deviceName, int recentSeconds) {
        List<SensorData> sensorDataList = sensorDataRepository.findAllRecentSensorDataWithDeviceName(deviceName, LocalDateTime.now().minusSeconds(recentSeconds));
        return sensorDataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean canDeleteSensorData(Authentication authentication, Integer sensorDataId) {
        // kiểm tra có phải là admin
        if (isAdmin(authentication)) {
            return true;
        }
        // kiểm tra đây có phải là user sở hữu device gắn với sensor data không
        if(checkUserAccessToSensorData(authentication, sensorDataId)) {
            return true;
        }
        return false;
    }

    public boolean canGetSensorData(Authentication authentication,Integer sensorDataId) {
        // kiểm tra có phải là admin
        if (isAdmin(authentication)) {
            return true;
        }
        // kiểm tra đây có phải là user sở hữu device gắn với sensor data không
        if(checkUserAccessToSensorData(authentication, sensorDataId)) {
            return true;
        }
        return false;
    }

    public boolean canPostSensorData(Authentication authentication, SensorData sensorData) {

        // kiểm tra đây có phải là device gắn với sensor data không
        if(checkDevice(authentication, sensorData.getDeviceId())) {
            return true;
        }
        // kiểm tra có phải là admin
        if (isAdmin(authentication)) {
            return true;
        }

        // kiểm tra đây có phải là user sở hữu device gắn với sensor data không
        if(checkUserAndDevice(authentication, sensorData.getDeviceId())) {
            return true;
        }
        return false;
    }


    public boolean canUpdateSensorData(Authentication authentication, Integer sensorDataId, SensorData sensorData) {

        // kiểm tra có phải là admin
        if (isAdmin(authentication)) {
            return true;
        }

        // kiểm tra đây có phải là user sở hữu device gắn với sensor data không
        if(checkUserAccessToSensorData(authentication, sensorDataId)) {
            return true;
        }
        return false;
    }

    private boolean checkUserAndDevice(Authentication authentication, Integer deviceId) {

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

        return deviceRepository.existsByIdAndUserId(deviceId, currentUserId);
    }
    private boolean isAdmin(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(auth -> auth.equals("ROLE_ADMIN"))
                .findFirst()
                .orElse(null);
        return role != null;
    }

    private boolean checkUserAccessToSensorData(Authentication authentication, Integer sensorDataId) {
        SensorData sensorData = sensorDataRepository.findById(sensorDataId)
                .orElseThrow(() -> new RuntimeException("Sensor data not found with id: " + sensorDataId));

        Device device = sensorData.getDevice();
        //
        if (device == null || !deviceService.isOwner(authentication, device.getId())) {
            return false;
        }

        return true;
    }

    private boolean checkDevice(Authentication authentication,  Integer deviceId) {
        if(authentication.getPrincipal() instanceof CustomUserDetails) {
            return false;
        }
        Integer DeviceId = (Integer) authentication.getPrincipal();
        if(DeviceId.equals(deviceId)) {
            return true;
        }
        return false;
    }


    // Chuyển đổi Entity sang DTO
     private SensorDataDTO convertToDTO(SensorData sensorData) {
         SensorDataDTO dto = new SensorDataDTO();
         dto.setId(sensorData.getId());
         dto.setDeviceId(sensorData.getDeviceId());
         dto.setType(sensorData.getType());
         dto.setValue(sensorData.getValue());
         dto.setReceivedAt(sensorData.getReceivedAt());

         if (sensorData.getDevice() != null) {
             dto.setDeviceName(sensorData.getDevice().getName());
         }

         return dto;
     }
 }
