package com.iot.controller;

import com.iot.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@CrossOrigin(origins = "*") // Quan trọng nếu Frontend khác port
public class SseController {

    @Autowired
    private SseService sseService;

    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // Gọi service để tạo và lưu emitter vào danh sách quản lý
        return sseService.createConnection();
    }
}