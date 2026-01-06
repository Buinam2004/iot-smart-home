package com.iot.controller;

import com.iot.entity.Fan;
import com.iot.entity.Led_Pir;
import com.iot.service.MqttPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fan")
public class FanController {

    @Autowired
    private MqttPublishService mqttPublishService;

    @PostMapping("/")
    public ResponseEntity<?> publishFan(@RequestBody Integer deviceId, @RequestBody int state) {
        try {
            mqttPublishService.sendFanCommand(deviceId.toString(), state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển quạt");
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

}
