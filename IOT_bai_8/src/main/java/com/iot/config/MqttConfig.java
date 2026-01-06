package com.iot.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String broker;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttClient subscriberClient() throws MqttException {
        String clientId = uniqueClientId("subscriber");
        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions options = buildOptions(false); // cleanSession = false để giữ subscription
        client.connect(options);

        log.info("MQTT Subscriber Client connected | ID: {} | Broker: {}", clientId, broker);
        return client;
    }

    @Bean
    public MqttClient publisherClient() throws MqttException {
        String clientId = uniqueClientId("publisher");
        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions options = buildOptions(true);
        client.connect(options);

        log.info("MQTT Publisher Client connected | ID: {} | Broker: {}", clientId, broker);
        return client;
    }

    private MqttConnectOptions buildOptions(boolean cleanSession) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(180);

        if (!username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        log.debug("MQTT Options: cleanSession={}, autoReconnect=true", cleanSession);
        return options;
    }

    private String uniqueClientId(String suffix) {
        return clientId + "_" + suffix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}