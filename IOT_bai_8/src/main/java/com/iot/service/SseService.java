package com.iot.service;


import com.iot.entity.DhtSensor;
import com.iot.entity.GasSensor;
import com.iot.entity.PirSensor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseService {
    // Map lưu trữ: DeviceID -> List các kết nối của thiết bị đó
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> deviceEmitters = new ConcurrentHashMap<>();

    public SseEmitter createConnection(String deviceId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Nếu chưa có list cho deviceId này thì tạo mới
        deviceEmitters.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Dọn dẹp khi kết thúc
        emitter.onCompletion(() -> removeEmitter(deviceId, emitter));
        emitter.onTimeout(() -> removeEmitter(deviceId, emitter));
        emitter.onError((e) -> removeEmitter(deviceId, emitter));

        log.info("Client đăng ký nhận data cho thiết bị: {}", deviceId);
        return emitter;
    }

    private void removeEmitter(String deviceId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = deviceEmitters.get(deviceId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                deviceEmitters.remove(deviceId);
            }
        }
    }

    public void broadcastDhtData(DhtSensor data) {
        String deviceId = String.valueOf(data.getDeviceId());
        sendToDevice(deviceId, "dht-data", data);
    }

    public void broadcastPirData(PirSensor data) {
        String deviceId = String.valueOf(data.getDeviceId());
        sendToDevice(deviceId, "pir-data", data);
    }

    public void broadcastGasData(GasSensor data) {
        String deviceId = String.valueOf(data.getDeviceId());
        sendToDevice(deviceId, "gas-data", data);
    }
    public void sendToDevice(String deviceId, String event, DhtSensor data) {}

    // Hàm dùng chung để gửi message cho đúng nhóm deviceId
    private void sendToDevice(String deviceId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = deviceEmitters.get(deviceId);
        if (emitters != null) {
            emitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(data, MediaType.APPLICATION_JSON));
                    return false;
                } catch (Exception e) {
                    return true;
                }
            });
        }
    }
}
