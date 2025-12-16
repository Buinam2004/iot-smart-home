#include "fan_control.h"
#include "pins.h"

void Fan_init() {
    pinMode(FAN_IN1, OUTPUT);
    pinMode(FAN_IN2, OUTPUT);

    Fan_off();
}

void Fan_on() {
    digitalWrite(FAN_IN1, HIGH);
    digitalWrite(FAN_IN2, LOW);
}

void Fan_off() {
    digitalWrite(FAN_IN1, LOW);
    digitalWrite(FAN_IN2, LOW);
}

void Fan_speed(uint8_t speed) {
    analogWrite(FAN_IN1, speed); // tốc độ
    digitalWrite(FAN_IN2, LOW);
}
