#include "pir_sensor.h"
#include "pins.h"

static bool motionDetected = false;
static bool lastMotionState = false;
static unsigned long lastMotionTime = 0;
static unsigned long timeoutMs = PIR_TIMEOUT_MS;

void PIR_init() {
    pinMode(PIR_PIN, INPUT);
    lastMotionTime = millis();
}

bool PIR_read() {
    lastMotionState = motionDetected;
    motionDetected = digitalRead(PIR_PIN) == HIGH;
    
    if (motionDetected) {
        lastMotionTime = millis();  // Reset timer mỗi khi có chuyển động
    }
    
    return motionDetected;
}

bool PIR_isMotionDetected() {
    return motionDetected;
}

bool PIR_hasNewMotion() {
    // Phát hiện cạnh lên (từ không có → có chuyển động)
    return motionDetected && !lastMotionState;
}

bool PIR_shouldTurnOffLight() {
    // Không có chuyển động VÀ đã quá timeout
    return !motionDetected && (millis() - lastMotionTime >= timeoutMs);
}

void PIR_resetTimer() {
    lastMotionTime = millis();
}

unsigned long PIR_getTimeSinceLastMotion() {
    return millis() - lastMotionTime;
}

void PIR_setTimeout(unsigned long timeout) {
    timeoutMs = timeout;
}
