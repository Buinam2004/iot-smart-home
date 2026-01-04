#include "button.h"
#include "pins.h"

static bool lastFanButtonState = HIGH;
static bool fanButtonPressed = false;
static bool fanButtonReleased = false;
static unsigned long lastDebounceTime = 0;

void Button_init() {
    // D8 (GPIO15) có pull-down nội bộ, nên dùng INPUT
    // Button nối giữa pin và 3.3V (nhấn = HIGH)
    pinMode(BTN_FAN_PIN, INPUT);
}

void Button_update() {
    bool currentState = digitalRead(BTN_FAN_PIN);
    
    // Reset trạng thái released
    fanButtonReleased = false;
    
    // Debounce
    if (currentState != lastFanButtonState) {
        lastDebounceTime = millis();
    }
    
    if ((millis() - lastDebounceTime) > DEBOUNCE_DELAY) {
        // Button được nhấn (HIGH vì GPIO15 có pull-down)
        if (currentState == HIGH && !fanButtonPressed) {
            fanButtonPressed = true;
        }
        // Button được nhả ra
        else if (currentState == LOW && fanButtonPressed) {
            fanButtonPressed = false;
            fanButtonReleased = true;  // Đánh dấu vừa nhả ra
        }
    }
    
    lastFanButtonState = currentState;
}

bool Button_fanPressed() {
    return fanButtonPressed;
}

bool Button_fanReleased() {
    // Trả về true một lần duy nhất khi button vừa được nhả ra
    if (fanButtonReleased) {
        fanButtonReleased = false;
        return true;
    }
    return false;
}

