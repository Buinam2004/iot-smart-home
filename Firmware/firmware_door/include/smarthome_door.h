#pragma once
#include <Arduino.h>

// Callback gửi MQTT
typedef void (*DoorDataCallback)(const char* topic, const char* payload);

// ===== INIT / LOOP =====
void ControlDoor_init();
void ControlDoor_update();

// ===== MQTT =====
void Door_setDataCallback(DoorDataCallback cb);
void Door_handleCommand(const char* payload);
