#pragma once
#include <Arduino.h>

void DHT_init();
bool DHT_read();                    // Đọc cả nhiệt độ và độ ẩm
float DHT_getTemperature();
float DHT_getHumidity();
bool DHT_isValid();                 // Kiểm tra giá trị có hợp lệ không
