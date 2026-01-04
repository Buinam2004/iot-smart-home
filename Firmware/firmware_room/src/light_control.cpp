#include "light_control.h"
#include "pins.h"

// ===== LED PIR State =====
static bool ledPIR_state = false;

// ===== LED GAS State =====
static bool ledGAS_state = false;
static bool ledGAS_blinking = false;
static bool ledGAS_currentOutput = false;
static unsigned long ledGAS_lastBlinkTime = 0;
static unsigned int ledGAS_blinkInterval = 250;

// ==================== LED PIR ====================

void LedPIR_init() {
    pinMode(LED_PIR_PIN, OUTPUT);
    LedPIR_off();
}

void LedPIR_on() {
    digitalWrite(LED_PIR_PIN, HIGH);
    ledPIR_state = true;
}

void LedPIR_off() {
    digitalWrite(LED_PIR_PIN, LOW);
    ledPIR_state = false;
}

bool LedPIR_isOn() {
    return ledPIR_state;
}

// ==================== LED GAS ====================

void LedGAS_init() {
    pinMode(LED_GAS_PIN, OUTPUT);
    LedGAS_off();
}

void LedGAS_on() {
    digitalWrite(LED_GAS_PIN, HIGH);
    ledGAS_state = true;
    ledGAS_blinking = false;
}

void LedGAS_off() {
    digitalWrite(LED_GAS_PIN, LOW);
    ledGAS_state = false;
    ledGAS_blinking = false;
}

void LedGAS_startBlinking(unsigned int interval) {
    ledGAS_blinking = true;
    ledGAS_blinkInterval = interval;
    ledGAS_lastBlinkTime = millis();
    ledGAS_currentOutput = true;
    digitalWrite(LED_GAS_PIN, HIGH);
}

void LedGAS_stopBlinking() {
    ledGAS_blinking = false;
    ledGAS_state = false;
    ledGAS_currentOutput = false;
    digitalWrite(LED_GAS_PIN, LOW);
}

void LedGAS_update() {
    if (ledGAS_blinking) {
        unsigned long now = millis();
        if (now - ledGAS_lastBlinkTime >= ledGAS_blinkInterval) {
            ledGAS_lastBlinkTime = now;
            ledGAS_currentOutput = !ledGAS_currentOutput;
            digitalWrite(LED_GAS_PIN, ledGAS_currentOutput ? HIGH : LOW);
        }
    }
}

bool LedGAS_isOn() {
    return ledGAS_state || ledGAS_blinking;
}

bool LedGAS_isBlinking() {
    return ledGAS_blinking;
}

