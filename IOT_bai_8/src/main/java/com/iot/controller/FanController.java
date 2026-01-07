package com.iot.controller;

import com.iot.dto.CommandRequest;
import com.iot.dto.FanStateResponse;
import com.iot.entity.Fan;
import com.iot.repository.FanRepository;
import com.iot.service.IFanService;
import com.iot.service.MqttPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fan")
@Slf4j
@RequiredArgsConstructor
public class FanController {
    private final MqttPublishService mqttPublishService;
    private final IFanService fanService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishFan(@RequestBody CommandRequest fanrequest) {
        Integer deviceId = fanrequest.getDeviceId();

        int state = fanrequest.getState();
        try {
            log.info("Gửi lệnh điều khiển quạt: deviceId={}, state={}", deviceId, state);
            mqttPublishService.sendFanCommand(deviceId, state);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển quạt");
        }
        catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getStateFan(@RequestParam Integer deviceId) {

        return fanService.getStateFan(deviceId)
                .map(fan -> ResponseEntity.ok(
                        new FanStateResponse(
                                fan.getDeviceId(),
                                fan.getState(),
                                fan.getCreatedAt()
                        )
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
