#include "light_control.h"
#include "pins.h"
#include <Arduino.h>

void Light_init() {
    pinMode(RELAY_PIN, OUTPUT);
    digitalWrite(RELAY_PIN, HIGH);
}

void Light_on() { digitalWrite(RELAY_PIN, LOW); }  
void Light_off() { digitalWrite(RELAY_PIN, HIGH); }
