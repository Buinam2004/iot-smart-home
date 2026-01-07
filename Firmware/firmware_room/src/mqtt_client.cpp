#include "mqtt_client.h"

#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

#include "smarthome_room.h"

// ===================================================
// MQTT CONFIG
// ===================================================
#define MQTT_HOST "broker.emqx.io"
#define MQTT_PORT 1883

#define MQTT_CLIENT_ID "spring-backend-room"
#define MQTT_USER "firmware_room"
#define MQTT_PASS "12345678"

#define MQTT_TOPIC "iot-smarthome/room1/8c4f00416f9e"

// ===================================================
// MQTT OBJECTS
// ===================================================
static WiFiClient net;
static PubSubClient mqtt(net);

// ===================================================
// MQTT CALLBACK (RECEIVE FROM BACKEND)
// ===================================================
static void mqttCallback(char* topic, byte* payload, unsigned int length) {
    String t(topic);
    String msg;

    for (unsigned int i = 0; i < length; i++) {
        msg += (char)payload[i];
    }

    Serial.printf("[MQTT] RX %s -> %s\n", t.c_str(), msg.c_str());

    // ===== PARSE JSON =====
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, msg);
    
    if (error) {
        Serial.print("[MQTT] JSON parse error: ");
        Serial.println(error.c_str());
        return;
    }

    // ===== XỬ LÝ COMMAND DỰA TRÊN TYPE =====
    const char* type = doc["type"];
    
    if (strcmp(type, "command") == 0) {
        const char* device = doc["device"];
        
        // ----- FAN CONTROL -----
        if (strcmp(device, "fan") == 0) {
            int state = doc["state"];
            Room_setFanState(state == 1);
        }
        
        // ----- GAS ALERT CLEAR -----
        else if (strcmp(device, "gas") == 0) {
            const char* action = doc["action"];
            if (strcmp(action, "clear") == 0) {
                Room_clearGasAlert();
            }
        }
        
        // ----- PIR LED CONTROL -----
        else if (strcmp(device, "led_pir") == 0) {
            int state = doc["state"];
            Room_setPIRLedState(state == 1);
        }
    }
}

// ===================================================
// INIT MQTT
// ===================================================
void MQTT_init() {
    mqtt.setServer(MQTT_HOST, MQTT_PORT);
    mqtt.setCallback(mqttCallback);

    Serial.println("[MQTT] Init done");
}

// ===================================================
// MQTT LOOP (CALL IN loop())
// ===================================================
void MQTT_loop() {
    if (!mqtt.connected()) {
        Serial.print("[MQTT] Connecting... ");

        if (mqtt.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASS)) {
            Serial.println("OK");

            // Subscribe single topic
            mqtt.subscribe(MQTT_TOPIC);
            Serial.printf("[MQTT] Subscribed: %s\n", MQTT_TOPIC);
        } else {
            Serial.print("FAILED, rc=");
            Serial.println(mqtt.state());
            delay(3000);
            return;
        }
    }

    mqtt.loop();
}

// ===================================================
// PUBLISH (USED BY Room CALLBACK)
// ===================================================
void MQTT_publish(const char* topic, const char* payload) {
    if (!mqtt.connected()) return;
    mqtt.publish(topic, payload);
}
