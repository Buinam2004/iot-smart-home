package com.iot.controller;

import com.iot.dto.CommandRequest;
import com.iot.service.MqttPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/led_pir")
@Slf4j
@RequiredArgsConstructor
public class Led_PirController {
    private final MqttPublishService mqttPublishService;

    @PostMapping("/publish")
    public ResponseEntity<?> pubishLed_Pir(@RequestBody CommandRequest fanrequest) {
        int deviceId = fanrequest.getDeviceId();
        int state = fanrequest.getState();
        try {
            mqttPublishService.sendLed_PirCommand(deviceId, state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển đèn led");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}
