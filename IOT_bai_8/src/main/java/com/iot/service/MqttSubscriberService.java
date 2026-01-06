package com.iot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.entity.*;
import com.iot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttSubscriberService implements MqttCallbackExtended {

    private final MqttClient subscriberClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DhtSensorRepository DhtSensorRepository;
    private final PirSensorRepository pirSensorRepository;
    private final DeviceRepository deviceRepository;
    private final GasSensorRepository gasSensorRepository;
    private final RfidRepository rfidRepository;
    private final DoorRepository doorRepository;
    private final FanRepository fanRepository;
    private final Led_PirRepository ledPirRepository;
    private final SseService sseService;
    private final MqttPublishService mqttPublishService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    private static final String[] TOPICS = {
            "iot_smarthome/door1/+",
            "iot_smarthome/room1/+"  // + = macAddress
    };

    private static final int[] QOS = {1, 1};

    @PostConstruct
    public void init() throws MqttException {
        subscriberClient.setCallback(this);
        subscribe();
        log.info("MqttSubscriberService đã khởi động và subscribe các topic");
    }

    @PreDestroy
    public void shutdown() throws MqttException {
        if (subscriberClient.isConnected()) {
            subscriberClient.disconnect();
            log.info("MQTT Subscriber đã disconnect sạch sẽ");
        }
    }

    private void subscribe() throws MqttException {
        subscriberClient.subscribe(TOPICS, QOS);
        log.info("Đã subscribe các topic: iot_smarthome/door1, iot_smarthome/room1");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT Subscriber {}connected thành công", reconnect ? "re" : "");
        try {
            subscribe(); // Resubscribe khi reconnect (rất quan trọng!)
        } catch (MqttException e) {
            log.error("Resubscribe thất bại sau reconnect", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Mất kết nối MQTT Subscriber: {}", cause.getMessage());
        // Automatic reconnect sẽ tự xử lý
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        String[] parts = topic.split("/");
        String macAddress = parts[2];
        String deviceId = null;
        try {
            Device device = deviceRepository.findByMacAddress(macAddress);
            if (device != null) {
                deviceId = String.valueOf(device.getId());
            }
            else {
                log.error("Device with MAC address {} not found", macAddress);
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        log.info("deviceId: {}", deviceId);
        log.info("Nhận message | topic: {} | payload: {}", topic, payload);


        // Gọi method xử lý chính
        handleMessage(topic, payload, deviceId);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Không dùng cho subscriber
    }

    public void handleMessage(String topic, String payload, String deviceId) {
        try {

            if( deviceId == null){
                return;
            }
            if(! deviceRepository.existsById(Integer.parseInt(deviceId))){
                return;
            }

            JsonNode json = objectMapper.readTree(payload);
            
            if (topic.contains("room1")) {
                handleRoom1Message(json, deviceId);
            } else if (topic.contains("door1")) {
                handleDoor1Message(json, deviceId);
            } else {
                log.warn("Unknown topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("Error parsing message from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    // ==================== ROOM1 MESSAGE HANDLERS ====================
    private void  handleRoom1Message(JsonNode json, String deviceId) {
        String type = json.has("type") ? json.get("type").asText() : "";
        
        switch (type) {
            case "sensor":
                handleSensorData(json, deviceId);
                break;
            case "gas":
                handleGasEvent(json, deviceId);
                break;
            case "device":
                handleDeviceState(json, deviceId);
                break;
            case "command":
                log.info("Command response from room: {}", json);
                break;
            default:
                log.warn("Unknown message type from room1: {}", type);
        }
    }

    private void handleSensorData(JsonNode json, String deviceId) {
        String sensorType = json.has("sensor") ? json.get("sensor").asText() : "";
        
        switch (sensorType) {
            case "dht":
                handleDHTData(json, deviceId);
                break;
            case "pir":
                handlePIRData(json, deviceId);
                break;
            default:
                log.warn("Unknown sensor type: {}", sensorType);
        }
    }

    private void handleDHTData(JsonNode json, String deviceId) {
        // TODO: Lưu vào database (InfluxDB/MySQL)
        // TODO: Kiểm tra ngưỡng và gửi thông báo nếu cần
        // TODO: Cập nhật dashboard real-time
        double temperature = json.get("temperature").asDouble();
        double humidity = json.get("humidity").asDouble();
        String type = json.has("type") ? json.get("type").asText() : "";
        String timestamp = json.get("timestamp").asText();
        String sensor = json.has("sensor") ? json.get("sensor").asText() : "";

        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER);
        log.info("📊 DHT22 | Temp: {}°C | Humidity: {}% | Time: {}", 
                temperature, humidity, timestamp);

        DhtSensor dhtSensor = new DhtSensor();
        dhtSensor.setDeviceId(Integer.parseInt(deviceId));
        dhtSensor.setTemperature(temperature);
        dhtSensor.setHumidity(humidity);
        dhtSensor.setType(type);
        dhtSensor.setSensor(sensor);
        dhtSensor.setReceivedAt(localDateTime);
        DhtSensorRepository.save(dhtSensor);

        sseService.broadcastDhtData(dhtSensor);
        
    }

    private void handlePIRData(JsonNode json, String deviceId) {
        // TODO: Lưu lịch sử chuyển động
        // TODO: Gửi notification nếu có chuyển động bất thường
        // TODO: Tích hợp với hệ thống an ninh
        int motion = json.get("motion").asInt();
        int light = json.get("light").asInt();
        String sensor = json.has("sensor") ? json.get("sensor").asText() : "";
        String timestamp = json.get("timestamp").asText();
        
        String motionStatus = motion == 1 ? "DETECTED" : "CLEAR";
        String lightStatus = light == 1 ? "ON" : "OFF";
        
        log.info("👤 PIR | Motion: {} | LED: {} | Time: {}", 
                motionStatus, lightStatus, timestamp);

        PirSensor pirSensor = new PirSensor();
        pirSensor.setDeviceId(Integer.parseInt(deviceId));
        pirSensor.setMotion(motion);
        pirSensor.setLight(light);
        pirSensor.setType(motionStatus);
        pirSensor.setSensor(sensor);
        pirSensorRepository.save(pirSensor);

        sseService.broadcastPirData(pirSensor);
    }

    private void handleGasEvent(JsonNode json, String deviceId) {
        String event = json.get("event").asText();
        int state = json.get("state").asInt();
        String type = json.get("type").asText();
        String timestamp = json.get("timestamp").asText();
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER);
        if ("alert".equals(event)) {
            int value = json.has("value") ? json.get("value").asInt() : 0;
            log.error("GAS ALERT! | Value: {} | Time: {}", value, timestamp);

            GasSensor gasSensor = new GasSensor();
            gasSensor.setDeviceId(Integer.parseInt(deviceId));
            gasSensor.setState(state);
            gasSensor.setValue(value);
            gasSensor.setEvent(event);
            gasSensor.setType(type);
            gasSensor.setReceivedAt(localDateTime);
            gasSensorRepository.save(gasSensor);

            sseService.broadcastGasData(gasSensor);

            // TODO: Gửi cảnh báo khẩn cấp (SMS, email, push notification)
            // TODO: Tự động gửi lệnh mở cửa để thông gió
            // TODO: Lưu vào database với mức độ ưu tiên cao
            // TODO: Kích hoạt còi báo động nếu có
            
        } else if ("clear".equals(event)) {
            log.info("Gas alert cleared | Time: {}", timestamp);
            
            // TODO: Gửi thông báo an toàn
            // TODO: Cập nhật trạng thái hệ thống
        }
    }

    private void handleDeviceState(JsonNode json, String deviceId) {
        String device = json.get("device").asText();
        int state = json.get("state").asInt();
        String timestamp = json.get("timestamp").asText();
        
        String stateStr = state == 1 ? "ON" : "OFF";
        
        switch (device) {
            case "fan":
                log.info("💨 Fan: {} | Time: {}", stateStr, timestamp);
                handleFanData(state, timestamp, deviceId);
                break;
            case "led_pir":
                log.info("💡 LED PIR: {} | Time: {}", stateStr, timestamp);
                handleLed_pir(state, timestamp, deviceId);
                break;
            default:
                log.info("🔧 Device '{}': {} | Time: {}", device, stateStr, timestamp);
        }
        
        // TODO: Lưu trạng thái thiết bị vào database
        // TODO: Cập nhật dashboard real-time
        // TODO: Tính toán thời gian hoạt động và tiêu thụ điện
    }

    private void handleFanData(int state, String timestamp, String deviceId) {
        Fan fan = new Fan();
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp);
        fan.setState(state);
        fan.setCreatedAt(localDateTime);
        fan.setDeviceId(Integer.parseInt(deviceId));

        fanRepository.save(fan);
        log.info("Dinh Quoc Dat 2");
         // publish
        sseService.broadcastFanData(fan);

    }
    private void handleLed_pir(int state, String timestamp, String deviceId) {
        Led_Pir led_pir = new Led_Pir();
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp);


        led_pir.setState(state);
        led_pir.setCreatedAt(localDateTime);
        led_pir.setDeviceId(Integer.parseInt(deviceId));

        ledPirRepository.save(led_pir);

        // publish
        sseService.broadcastLed_PirData(led_pir);
    }






    // ==================== DOOR1 MESSAGE HANDLERS ====================
    private void handleDoor1Message(JsonNode json, String deviceId) {
        // TODO: Implement door message handling based on door firmware specs
        log.info("🚪 Door message: {}", json);

        String type = json.has("type") ? json.get("type").asText() : "";
        String event = json.has("event") ? json.get("event").asText() : "";
        String uid = json.has("uid") ? json.get("uid").asText() : "";
        String timestamp = json.has("timestamp") ? json.get("timestamp").asText() : "";
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER);
        if(rfidRepository.existsByUidAndDeviceId(uid, Integer.parseInt(deviceId))){
            Door door = new Door();
            door.setDeviceId(Integer.parseInt(deviceId));
            door.setUid(uid);
            door.setEvent(event);
            door.setType(type);
            door.setAction("OPEN");
            door.setReceiveAt(localDateTime);
            doorRepository.save(door);

            // Đẩy event vào MQTT
            try {
                mqttPublishService.sendDoorCommand(deviceId, "OPEN");
            }
            catch (MqttException e) {
                e.printStackTrace();
            }

        }
        else {
            Door door = new Door();
            door.setDeviceId(Integer.parseInt(deviceId));
            door.setUid(uid);
            door.setEvent(event);
            door.setType(type);
            door.setAction("DENY");
            door.setReceiveAt(localDateTime);
            doorRepository.save(door);
            // Đẩy event vào MQTT
            try{
                mqttPublishService.sendDoorCommand(deviceId, "DENY");
            }
            catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

}