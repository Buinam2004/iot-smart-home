package com.iot.controller;

import com.iot.entity.Led_Pir;
import com.iot.service.MqttPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/led_pir")
public class Led_PirController {

    @Autowired
    private MqttPublishService mqttPublishService;

    @PostMapping
    public ResponseEntity<?> pubishLed_Pir(@RequestBody Integer deviceId, @RequestBody int state) {
        try {
            mqttPublishService.sendLed_PirCommand(deviceId.toString(), state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển đèn led");
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }

    }
}
