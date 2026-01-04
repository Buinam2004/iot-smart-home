#include "fan_control.h"
#include "pins.h"

static bool fanState = false;

void Fan_init() {
    pinMode(FAN_INA_PIN, OUTPUT);
    pinMode(FAN_INB_PIN, OUTPUT);
    
    // Đảm bảo quạt tắt khi khởi động
    Fan_off();
}

void Fan_on() {
    // L9110H: INA=HIGH, INB=LOW → Quay thuận
    digitalWrite(FAN_INA_PIN, HIGH);
    digitalWrite(FAN_INB_PIN, LOW);
    fanState = true;
}

void Fan_off() {
    // L9110H: INA=LOW, INB=LOW → Dừng
    digitalWrite(FAN_INA_PIN, LOW);
    digitalWrite(FAN_INB_PIN, LOW);
    fanState = false;
}

void Fan_toggle() {
    if (fanState) {
        Fan_off();
    } else {
        Fan_on();
    }
}

void Fan_setState(bool state) {
    if (state) {
        Fan_on();
    } else {
        Fan_off();
    }
}

bool Fan_getState() {
    return fanState;
}

