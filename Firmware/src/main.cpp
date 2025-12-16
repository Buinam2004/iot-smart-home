#include <Arduino.h>
#include "smarthome.h"
#include <lcd_display.h>

SmartHome home;

unsigned long lastLCDUpdate = 0;

void setup() {
    Serial.begin(115200);
    home.begin();
    home.show("System Ready");
}

void loop() {
    // RFID
    String uid = home.readRFID();
    if (uid != "") {
        home.show("RFID Scan:", uid);
        delay(1500); // hiển thị 1.5s
    }

    // Keypad
    char key = home.readKey();
    if (key) {
        home.show("Key:", String(key));
        delay(800);
    }

    // Update LCD sensor mỗi 500ms
    if (millis() - lastLCDUpdate > 500) {
        home.showTempHum();  // <-- GỌI API MỚI
        lastLCDUpdate = millis();
    }

    // PIR
    if (home.motion()) {
        Serial.println("Motion detected!");
    }

    // GAS
    if (home.gas()) {
        Serial.println("GAS HIGH -> LIGHT ON!");
        home.lightOn();
    }

    // FAN auto
    if (home.getTemp() > 30) home.fanOn();
    else home.fanOff();

    delay(20);
}
