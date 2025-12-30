package com.iot.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);

    private final MqttClient mqttClient;

    @Value("${mqtt.topic.sensors:sensors/+/data}")
    private String sensorsTopic;

    @Value("${mqtt.qos:1}")
    private int qos;

    public MqttSubscriber(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @PostConstruct
    public void subscribe() {
        try {
            if (!mqttClient.isConnected()) {
                log.error("❌ MQTT Client is not connected. Cannot subscribe.");
                return;
            }

            // Subscribe to sensors topic with callback
            mqttClient.subscribe(sensorsTopic, qos, (topic, message) -> {
                try {
                    String payload = new String(message.getPayload());
                    log.info("📥 Received message on topic [{}]: {}", topic, payload);
                    
                    // TODO: Process sensor data (parse JSON, save to database, etc.)
                    processSensorData(topic, payload);
                    
                } catch (Exception e) {
                    log.error("❌ Error processing message from topic [{}]: {}", topic, e.getMessage());
                }
            });

            log.info("✅ Successfully subscribed to topic: {} with QoS {}", sensorsTopic, qos);
            
        } catch (MqttException e) {
            log.error("❌ Failed to subscribe to topic [{}]. Error: {}", sensorsTopic, e.getMessage());
        }
    }

    /**
     * Subscribe to a specific topic
     * @param topic MQTT topic to subscribe to
     * @throws MqttException if subscription fails
     */
    public void subscribeToTopic(String topic) throws MqttException {
        if (!mqttClient.isConnected()) {
            log.error("❌ MQTT Client is not connected. Cannot subscribe to topic: {}", topic);
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        mqttClient.subscribe(topic, qos, (t, message) -> {
            String payload = new String(message.getPayload());
            log.info("📥 Received message on topic [{}]: {}", t, payload);
        });

        log.info("✅ Subscribed to topic: {}", topic);
    }

    /**
     * Unsubscribe from a topic
     * @param topic MQTT topic to unsubscribe from
     * @throws MqttException if unsubscribe fails
     */
    public void unsubscribeFromTopic(String topic) throws MqttException {
        mqttClient.unsubscribe(topic);
        log.info("🚫 Unsubscribed from topic: {}", topic);
    }

    /**
     * Process sensor data - override this method or inject a service
     * @param topic MQTT topic
     * @param payload Message payload
     */
    private void processSensorData(String topic, String payload) {
        // TODO: Implement sensor data processing
        // Example: Parse JSON, validate, save to database
        log.debug("Processing sensor data from topic: {}", topic);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("🔌 MQTT Client disconnected");
            }
        } catch (MqttException e) {
            log.error("❌ Error disconnecting MQTT client: {}", e.getMessage());
        }
    }
}