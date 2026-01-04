#include "mq2_sensor.h"
#include "pins.h"

static int rawValue = 0;
static int threshold = GAS_THRESHOLD;
static bool alertActive = false;

void MQ2_init() {
    pinMode(MQ2_PIN, INPUT);
}

int MQ2_read() {
    rawValue = analogRead(MQ2_PIN);
    
    // Tự động kích hoạt alert nếu vượt ngưỡng
    if (rawValue >= threshold && !alertActive) {
        alertActive = true;
    }
    
    return rawValue;
}

int MQ2_getValue() {
    return rawValue;
}

void MQ2_setThreshold(int thresh) {
    threshold = thresh;
}

int MQ2_getThreshold() {
    return threshold;
}

bool MQ2_isGasDetected() {
    return rawValue >= threshold;
}

bool MQ2_isAlertActive() {
    return alertActive;
}

void MQ2_activateAlert() {
    alertActive = true;
}

void MQ2_deactivateAlert() {
    alertActive = false;
}
