#pragma once
#include <Arduino.h>

// ===== Callback gửi dữ liệu MQTT =====
typedef void (*DataCallback)(const char* topic, const char* payload);

// ===== Trạng thái hệ thống =====
enum SystemState {
    STATE_NORMAL,
    STATE_GAS_ALERT
};

// ===== Init / Loop =====
void Room_init();
void Room_update();

// ===== MQTT callback =====
void Room_setDataCallback(DataCallback callback);

// ===== Điều khiển từ WEB =====
void Room_setFanState(bool on);
void Room_clearGasAlert();
void Room_setPIRLedState(bool on);  // Điều khiển đèn PIR qua MQTT

// ===== Getter (nếu backend cần) =====
double Room_getTemperature();
double Room_getHumidity();
int   Room_getGasValue();
bool  Room_isFanOn();
bool  Room_isLightOn();
bool  Room_isGasAlert();
bool  Room_isMotionDetected();
SystemState Room_getState();
