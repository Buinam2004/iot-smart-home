#include "mqtt_client.h"

#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#include "smarthome_door.h"

#define MQTT_HOST "broker.emqx.io"
#define MQTT_PORT 1883

#define MQTT_CLIENT_ID "spring-backend-door"
#define MQTT_USER "firmware_door"
#define MQTT_PASS "12345678"

#define MQTT_TOPIC "iot-smarthome/door1/8c4f0042001f"
#define MQTT_TOPIC_ROOM "iot-smarthome/room1/8c4f00416f9e"

static WiFiClient net;
static PubSubClient mqtt(net);

static void mqttCallback(char* topic, byte* payload, unsigned int length) {
    char msg[256];
    memcpy(msg, payload, length);
    msg[length] = '\0';

    Serial.printf("[MQTT] RX %s <- %s\n", topic, msg);

    // Từ door1 (command từ backend)
    if (strcmp(topic, MQTT_TOPIC) == 0) {
        Door_handleCommand(msg);
        return;
    }

    // Từ room1 (gas alert, DHT data)
    if (strcmp(topic, MQTT_TOPIC_ROOM) == 0) {
        Door_handleCommand(msg);
        return;
    }
}

void MQTT_init() {
    mqtt.setServer(MQTT_HOST, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
}


void MQTT_loop() {
    if (!mqtt.connected()) {
        static unsigned long lastTry = 0;
        if (millis() - lastTry > 3000) {
            lastTry = millis();

            Serial.println("[MQTT] Attempting connection...");
            if (mqtt.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASS)) {
                Serial.println("[MQTT] Connected");

                // Subscribe topic chính
                mqtt.subscribe(MQTT_TOPIC);
                mqtt.subscribe(MQTT_TOPIC_ROOM);
                Serial.printf("[MQTT] Subscribed to %s & %s\n", MQTT_TOPIC, MQTT_TOPIC_ROOM);
            } else {
                Serial.print("[MQTT] Connection failed, state: ");
                Serial.println(mqtt.state());
                // States: -4=timeout, -3=lost, -2=failed, -1=disconnected, 0=connected
                //         1=bad protocol, 2=bad client id, 3=unavailable, 4=bad credentials, 5=unauthorized
            }
        }
        return;
    }
    mqtt.loop();
}


void MQTT_publish(const char* topic, const char* payload) {
    if (!mqtt.connected()) {
        // Thử kết nối lại ngay khi cần publish
        static unsigned long lastTry = 0;
        if (millis() - lastTry > 1000) {
            lastTry = millis();
            if (mqtt.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASS)) {
                mqtt.subscribe(MQTT_TOPIC);
                mqtt.subscribe(MQTT_TOPIC_ROOM);
                Serial.println("[MQTT] Reconnected for publish");
            } else {
                Serial.println("[MQTT] ERROR: Not connected, cannot publish");
                return;
            }
        } else {
            Serial.println("[MQTT] ERROR: Not connected, cannot publish");
            return;
        }
    }
    Serial.printf("[MQTT] TX %s -> %s\n", topic, payload);
    mqtt.publish(topic, payload);
}
