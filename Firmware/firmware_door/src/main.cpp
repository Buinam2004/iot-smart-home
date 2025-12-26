
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include "pins.h"
#include "lcd_display.h"
#include "smarthome_door.h"
#include "mqtt_client.h"

#define WIFI_SSID "IOT"
#define WIFI_PASS "20225211"

// MQTT callback gửi dữ liệu lên server nếu cần
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


void setup() {
    Serial.begin(115200);
    delay(200);

    Serial.println();
    Serial.println("===== DOOR CONTROLLER (ESP8266) =====");

    // WiFi
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

    // Door system init
    ControlDoor_init();
    Door_setDataCallback(mqttDataCallback);
    Serial.println("[Door] Initialized");

    // MQTT
    MQTT_init();
    Serial.println("[MAIN] MQTT_init done");

    Serial.println("[System] Setup complete");
}

void loop() {
    ControlDoor_update();
    MQTT_loop();
}
