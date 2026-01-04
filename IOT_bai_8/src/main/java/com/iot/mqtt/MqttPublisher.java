package com.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisher.class);

    private final MqttClient mqttClient;

    @Value("${mqtt.qos:1}")
    private int qos;

    public MqttPublisher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    /**
     * Publish message to MQTT topic
     * @param topic MQTT topic to publish to
     * @param payload Message payload
     * @throws MqttException if publish fails
     */
    public void publish(String topic, String payload) throws MqttException {
        if (!mqttClient.isConnected()) {
            log.error("❌ MQTT Client is not connected. Cannot publish to topic: {}", topic);
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(false);

            mqttClient.publish(topic, message);
            log.info("📤 Published to topic [{}]: {}", topic, payload);
        } catch (MqttException e) {
            log.error("❌ Failed to publish to topic [{}]. Error: {}", topic, e.getMessage());
            throw e;
        }
    }

    /**
     * Publish message with custom QoS
     * @param topic MQTT topic
     * @param payload Message payload
     * @param qosLevel Quality of Service level (0, 1, or 2)
     * @throws MqttException if publish fails
     */
    public void publish(String topic, String payload, int qosLevel) throws MqttException {
        if (!mqttClient.isConnected()) {
            log.error("❌ MQTT Client is not connected. Cannot publish to topic: {}", topic);
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qosLevel);
            message.setRetained(false);

            mqttClient.publish(topic, message);
            log.info("📤 Published to topic [{}] with QoS {}: {}", topic, qosLevel, payload);
        } catch (MqttException e) {
            log.error("❌ Failed to publish to topic [{}]. Error: {}", topic, e.getMessage());
            throw e;
        }
    }
}