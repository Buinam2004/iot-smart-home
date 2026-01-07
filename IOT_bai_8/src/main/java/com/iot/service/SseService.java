package com.iot.service;


import com.iot.entity.*;
import com.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> deviceEmitters = new ConcurrentHashMap<>();
    private final DeviceRepository deviceRepository;

    public SseEmitter createConnection(String deviceId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        deviceEmitters.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(emitter);

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
        Device device = deviceRepository.findById(data.getDeviceId()).orElseThrow(RuntimeException::new);
        String deviceId = String.valueOf(device.getMacAddress());
        sendToDevice(deviceId, "dht-data", data);
    }

    public void broadcastPirData(PirSensor data) {
        Device device = deviceRepository.findById(data.getDeviceId()).orElseThrow(RuntimeException::new);
        String deviceId = String.valueOf(device.getMacAddress());
        sendToDevice(deviceId, "pir-data", data);
    }

    public void broadcastGasData(GasSensor data) {
        Device device = deviceRepository.findById(data.getDeviceId()).orElseThrow(RuntimeException::new);
        String deviceId = String.valueOf(device.getMacAddress());
        sendToDevice(deviceId, "gas-data", data);
    }

    public void broadcastFanData(Fan data) {
        Device device = deviceRepository.findById(data.getDeviceId()).orElseThrow(RuntimeException::new);
        String deviceId = String.valueOf(device.getMacAddress());
        sendToDevice(deviceId, "fan-data", data);
    }

    public void broadcastLed_PirData(Led_Pir data){
        Device device = deviceRepository.findById(data.getDeviceId()).orElseThrow(RuntimeException::new);
        String deviceId = String.valueOf(device.getMacAddress());
        sendToDevice(deviceId, "led-pir-data", data);
    }

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
