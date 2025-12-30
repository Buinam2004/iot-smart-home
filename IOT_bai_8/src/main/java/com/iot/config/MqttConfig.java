package com.iot.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker.url}")
    private String broker;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.clean.session:true}")
    private boolean cleanSession;

    @Value("${mqtt.connection.timeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keep.alive.interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.max.inflight:100}")
    private int maxInflight;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        log.info("Creating MQTT Client with ID: {} for broker: {}", clientId, broker);
        
        MqttClient client = new MqttClient(broker, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setMaxInflight(maxInflight);

        if (username != null && !username.isEmpty()) {
            log.info("Setting MQTT authentication for user: {}", username);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        } else {
            log.info("Connecting to MQTT broker without authentication");
        }

        try {
            client.connect(options);
            log.info("✅ MQTT Client successfully connected to broker: {}", broker);
        } catch (MqttException e) {
            log.error("❌ Failed to connect to MQTT broker: {}. Error: {}", broker, e.getMessage());
            throw e;
        }

        return client;
    }
}
