#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <time.h>

#include "smarthome_room.h"
#include "mqtt_client.h"

// ===================================================
// WIFI CONFIG
// ===================================================
#define WIFI_SSID "IOT"
#define WIFI_PASS "20225211"

// ===================================================
// MQTT DATA CALLBACK
// Room -> MQTT
// ===================================================
static void mqttDataCallback(const char* topic, const char* payload) {
    MQTT_publish(topic, payload);
}
void syncTime() {
    configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
    Serial.print("[Time] Syncing");

    time_t now = time(nullptr);
    while (now < 100000) {   // chưa có time hợp lệ
        delay(500);
        Serial.print(".");
        now = time(nullptr);
    }

    Serial.println();
    Serial.println("[Time] Time synced");
}

// ===================================================
// SETUP
// ===================================================
void setup() {
    Serial.begin(115200);
    delay(200);

    Serial.println();
    Serial.println("===== ROOM CONTROLLER (ESP8266) =====");

    // ----- WIFI -----
    Serial.print("[WiFi] Connecting to ");
    Serial.println(WIFI_SSID);

    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    
    syncTime(); 
    Serial.println();
    Serial.println("[WiFi] Connected");
    Serial.print("[WiFi] IP: ");
    Serial.println(WiFi.localIP());

    // ----- ROOM INIT -----
    Room_init();

    // Register callback to send data via MQTT
    Room_setDataCallback(mqttDataCallback);

    // ----- MQTT INIT -----
    MQTT_init();

    Serial.println("[System] Setup complete");
}

// ===================================================
// LOOP
// ===================================================
void loop() {
    // Main logic
    Room_update();

    // MQTT handling (connect, subscribe, receive)
    MQTT_loop();
}
