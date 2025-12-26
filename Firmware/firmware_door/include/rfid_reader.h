#pragma once
#include <Arduino.h>

// Khởi tạo RFID
void RFID_init();

// Đọc thẻ (trả về true khi có thẻ mới)
bool RFID_read();

// Lấy UID dạng string (HEX)
String RFID_getUID();
