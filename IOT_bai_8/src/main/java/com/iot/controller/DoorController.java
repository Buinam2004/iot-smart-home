package com.iot.controller;

import com.iot.dto.DoorCommandRequest;
import com.iot.dto.DoorStateResponse;
import com.iot.entity.Door;
import com.iot.repository.DoorRepository;
import com.iot.service.IDoorService;
import com.iot.service.MqttPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/door")
@Slf4j
@RequiredArgsConstructor
public class DoorController {
    private final MqttPublishService mqttPublishService;
    private final DoorRepository doorRepository;
    private final IDoorService doorService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishFan(@RequestBody DoorCommandRequest doorRequest) {
        Integer deviceId = doorRequest.getDeviceId();

        int state = doorRequest.getState();
        String action = doorRequest.getAction();
        try {
            log.info("Gửi lệnh điều khiển cửa: deviceId={}, state={}", deviceId, state);
            Door door = new Door();
            door.setDeviceId(deviceId);
            door.setAction(action);
            door.setReceiveAt(LocalDateTime.now());
            doorRepository.save(door);

            mqttPublishService.sendDoorCommand(deviceId, action);
            return ResponseEntity.ok("Đã gửi lệnh điều khiển cửa");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getStateDoor(@RequestParam Integer deviceId) {

        return doorService.getStateDoor(deviceId)
                .map(door -> ResponseEntity.ok(
                        new DoorStateResponse(
                                door.getDeviceId(),
                                door.getAction(),
                                door.getReceiveAt()
                        )
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}