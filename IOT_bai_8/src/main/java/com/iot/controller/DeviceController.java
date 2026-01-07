package com.iot.controller;

import com.iot.custom.CustomUserDetails;
import com.iot.dto.CreateDeviceDTO;
import com.iot.dto.DeviceDTO;
import com.iot.dto.UpdateDeviceDTO;
import com.iot.entity.Device;
import com.iot.exception.ResourceNotFoundException;
import com.iot.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeviceController {
    private final IDeviceService deviceService;
    
    // GET /api/devices?creatorName=username - Tìm kiếm theo tên người tạo và trả về
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceDTO>> getAllDevices(
            @RequestParam(required = false) String creatorName,
            @RequestParam(required = false) Boolean status) {
        
        List<DeviceDTO> devices;

        if (creatorName == null || creatorName.isEmpty()) {
            devices = deviceService.getAllDevices();
            return ResponseEntity.ok(devices);
        }
        if (status) {
            devices = deviceService.findByCreatorUsernameAndStatus(creatorName, "online");
        } else {
            devices = deviceService.findByCreatorUsernameAndStatus(creatorName, "offline");
        }
        
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/user")
    public ResponseEntity<List<DeviceDTO>> getAllUserDevices(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(deviceService.getAllDeviceByUserId(customUserDetails.getUserId()));
    }

    // GET /api/devices/{id} - Lấy device theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceService.isOwner(authentication, #id)")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Integer id) {
        DeviceDTO device = deviceService.getDeviceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        return ResponseEntity.ok(device);
    }
    
    // POST /api/devices - Tạo device mới
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody CreateDeviceDTO device) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId(); // lấy userId từ CustomUserDetails
        Device createdDevice = deviceService.createDevice(userId, device);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }
    
    // PATCH /api/devices/{id} - Cập nhật device
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceService.isOwner(authentication, #id)")
    public ResponseEntity<UpdateDeviceDTO> updateDevice(@PathVariable Integer id, @RequestBody Device deviceDetails) {
        UpdateDeviceDTO updatedDevice = deviceService.updateDevice(id, deviceDetails);
        return ResponseEntity.ok(updatedDevice);
    }
    
    // DELETE /api/devices/{id} - Xóa device
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @deviceService.isOwner(authentication, #id)")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
