package com.iot.controller;

import com.iot.dto.CommandRequest;
import com.iot.dto.Led_PirStateResponse;
import com.iot.service.ILed_PirService;
import com.iot.service.MqttPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/led_pir")
@Slf4j
@RequiredArgsConstructor
public class Led_PirController {
    private final MqttPublishService mqttPublishService;
    private final ILed_PirService led_PirService;

    @PostMapping("/publish")
    public ResponseEntity<?> pubishLed_Pir(@RequestBody CommandRequest fanrequest) {
        Integer deviceId = fanrequest.getDeviceId();
        int state = fanrequest.getState();
        try {
            log.info("Gửi lệnh điều khiển đèn led: deviceId={}, state={}", deviceId, state);
            mqttPublishService.sendLed_PirCommand(deviceId, state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển đèn led");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getStateLed_Pir(@RequestParam Integer deviceId) {

        return led_PirService.getStateLed_Pir(deviceId)
                .map(led_pir -> ResponseEntity.ok(
                        new Led_PirStateResponse(
                                led_pir.getDeviceId(),
                                led_pir.getState(),
                                led_pir.getCreatedAt()
                        )
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
