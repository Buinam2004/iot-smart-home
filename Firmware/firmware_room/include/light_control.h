#pragma once
#include <Arduino.h>

// ===== LED PIR - Đèn tự động theo chuyển động =====
void LedPIR_init();
void LedPIR_on();
void LedPIR_off();
bool LedPIR_isOn();

// ===== LED GAS - Đèn cảnh báo khí gas (nhấp nháy) =====
void LedGAS_init();
void LedGAS_on();
void LedGAS_off();
void LedGAS_startBlinking(unsigned int interval = 250);
void LedGAS_stopBlinking();
void LedGAS_update();           // Gọi trong loop() để xử lý blink
bool LedGAS_isOn();
bool LedGAS_isBlinking();

