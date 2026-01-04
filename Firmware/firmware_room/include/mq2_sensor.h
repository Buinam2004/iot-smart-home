#pragma once
#include <Arduino.h>

void MQ2_init();
int MQ2_read();
int MQ2_getValue();
void MQ2_setThreshold(int thresh);
int MQ2_getThreshold();
bool MQ2_isGasDetected();           // Vượt ngưỡng
bool MQ2_isAlertActive();           // Đang trong trạng thái alert
void MQ2_activateAlert();
void MQ2_deactivateAlert();         // Chỉ gọi khi nhận lệnh từ server
