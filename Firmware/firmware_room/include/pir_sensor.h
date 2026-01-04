#pragma once
#include <Arduino.h>

void PIR_init();
bool PIR_read();                    // Đọc trạng thái PIR
bool PIR_isMotionDetected();        // Trạng thái hiện tại
bool PIR_hasNewMotion();            // Có chuyển động MỚI (rising edge)
bool PIR_shouldTurnOffLight();      // Đã hết timeout, nên tắt đèn
void PIR_resetTimer();              // Reset timer khi phát hiện chuyển động
unsigned long PIR_getTimeSinceLastMotion();
void PIR_setTimeout(unsigned long timeout);
