#pragma once
#include <Arduino.h>

// ===== INIT / LOOP =====
void MQTT_init();
void MQTT_loop();

// ===== PUBLISH =====
void MQTT_publish(const char* topic, const char* payload);
