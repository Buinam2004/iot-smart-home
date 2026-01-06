package com.iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.dto.DoorCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttPublishService {

    private final MqttClient publisherClient;
    private final ObjectMapper objectMapper;

    public void publish(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        message.setRetained(false);

        publisherClient.publish(topic, message);
        log.info("Đã publish | topic: {} | payload: {}", topic, payload);
    }

    public void sendDoorCommand(String deviceId, String action) throws MqttException {
        DoorCommand command = new DoorCommand("command", "door", action.toUpperCase());
        try {
            // 2. Chuyển Object thành JSON String
            String payload = objectMapper.writeValueAsString(command);

            // 3. Xác định topic (ví dụ: iot_smarthome/101/door1/command)
            String topic = String.format("iot_smarthome/%s/door1/command", deviceId);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);

            publisherClient.publish(topic, message);
            log.info("🚀 Sent Command | Topic: {} | Payload: {}", topic, payload);

        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON: {}", e.getMessage());
        } catch (MqttException e) {
            log.error("Lỗi Publish MQTT: {}", e.getMessage());
        }
    }
}