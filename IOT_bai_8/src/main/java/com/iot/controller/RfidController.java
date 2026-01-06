package com.iot.controller;

import com.iot.custom.CustomUserDetails;
import com.iot.dto.RfidDTO;
import com.iot.entity.Rfid;
import com.iot.service.RfidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfid")
@CrossOrigin(origins = "*")
public class RfidController {

    @Autowired
    private RfidService rfidService;

    @PostMapping
    public ResponseEntity<Rfid> CreateRfid(@RequestBody RfidDTO rfidDTO) {
        String uid = rfidDTO.getUid();
        int deviceId = rfidDTO.getDeviceId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId(); // lấy userId từ CustomUserDetails
        Rfid rfid = rfidService.createRfid(uid, deviceId ,userId);
        return ResponseEntity.ok(rfid);
    }
}
