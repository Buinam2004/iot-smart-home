#include "pir_sensor.h"
#include "pins.h"
#include <Arduino.h>

void PIR_init() {
    pinMode(PIR_PIN, INPUT);
}

bool PIR_detected() {
    return digitalRead(PIR_PIN);
}
