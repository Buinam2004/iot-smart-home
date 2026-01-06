package com.iot.service;


import com.iot.entity.DhtSensor;
import com.iot.entity.PirSensor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseService {
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createConnection() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Giữ kết nối lâu dài
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    public void broadcastDhtData(DhtSensor data) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("dht-data")
                        .data(data, MediaType.APPLICATION_JSON));
                return false; // Vẫn sống, không xóa
            } catch (Exception e) {
                log.warn("Lỗi gửi SSE, đang xóa emitter...");
                return true; // Xóa emitter này khỏi danh sách
            }
        });
    }
    public void broadcastPirData(PirSensor data) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("pir-data")
                        .data(data, MediaType.APPLICATION_JSON));
                return false; // Vẫn sống, không xóa
            } catch (Exception e) {
                log.warn("Lỗi gửi SSE, đang xóa emitter...");
                return true; // Xóa emitter này khỏi danh sách
            }
        });
    }
}
