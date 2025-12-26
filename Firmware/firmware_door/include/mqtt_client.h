#pragma once
#include <Arduino.h>

void MQTT_init();
void MQTT_loop();
void MQTT_publish(const char* topic, const char* payload);
