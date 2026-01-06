package com.iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.dto.DeviceState;
import com.iot.dto.DoorCommand;
import com.iot.entity.Device;
import com.iot.entity.Fan;
import com.iot.entity.Led_Pir;
import com.iot.repository.DeviceRepository;
import com.iot.repository.DoorRepository;
import com.iot.repository.FanRepository;
import com.iot.repository.Led_PirRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttPublishService {

    private final MqttClient publisherClient;
    private final ObjectMapper objectMapper;
    private final FanRepository fanRepository;
    private final DoorRepository doorRepository;
    private final Led_PirRepository led_PirRepository;
    private final DeviceRepository deviceRepository;

    public void sendDoorCommand(String macAddress, String action) throws MqttException {
        DoorCommand command = new DoorCommand("command", "door", action.toUpperCase());
        try {
            // 2. Chuyển Object thành JSON String
            String payload = objectMapper.writeValueAsString(command);

            String topic = String.format("iot_smarthome/door1/%s", macAddress);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(true);

            publisherClient.publish(topic, message);
            log.info("🚀 Sent Command | Topic: {} | Payload: {}", topic, payload);

        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON: {}", e.getMessage());
        } catch (MqttException e) {
            log.error("Lỗi Publish MQTT: {}", e.getMessage());
        }
    }

    public void sendFanCommand(Integer deviceId, int state) throws MqttException {
        try{
            Optional<Device> device = deviceRepository.findById(deviceId);
            if(device == null){
                log.error("Device with ID {} not found", deviceId);
                return;
            }
            log.info("Dinh Quoc Dat");
            String macAddress = device.get().getMacAddress();
            LocalDateTime  now = LocalDateTime.now();
            DeviceState deviceState = new DeviceState("device", "fan", state, now);
            String payload = objectMapper.writeValueAsString(deviceState);

            String topic = String.format("iot_smarthome/room1/%s", macAddress);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(true);

            publisherClient.publish(topic, message);
            log.info("🚀 Sent Command | Topic: {} | Payload: {}", topic, payload);
            Fan fan = new Fan();
            fan.setCreatedAt(now);
            fan.setDeviceId(deviceId);
            fan.setState(state);
            fanRepository.save(fan);

        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON: {}", e.getMessage());
        } catch (MqttException e) {
            log.error("Lỗi Publish MQTT: {}", e.getMessage());
        }
    }

    public void sendLed_PirCommand(int deviceId, int state) throws MqttException {
        try{
            Optional<Device> device = deviceRepository.findById(deviceId);
            if(device == null){
                log.error("Device with ID {} not found", deviceId);
                return;
            }
            String macAddress = device.get().getMacAddress();
            LocalDateTime  now = LocalDateTime.now();
            DeviceState deviceState = new DeviceState("device", "led_pir", state, now);
            String payload = objectMapper.writeValueAsString(deviceState);

            String topic = String.format("iot_smarthome/room1/%s", macAddress);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(true);

            publisherClient.publish(topic, message);
            log.info("🚀 Sent Command | Topic: {} | Payload: {}", topic, payload);
            Led_Pir led_pir = new Led_Pir();
            led_pir.setCreatedAt(now);
            led_pir.setDeviceId(deviceId);
            led_pir.setState(state);
            led_PirRepository.save(led_pir);

        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON: {}", e.getMessage());
        } catch (MqttException e) {
            log.error("Lỗi Publish MQTT: {}", e.getMessage());
        }
    }

}