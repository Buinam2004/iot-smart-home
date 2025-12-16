#pragma once
#include <Arduino.h>

class SmartHome {
public:
    void begin();

    // Inputs
    String readRFID();
    char readKey();
    float getTemp();
    float getHum();
    bool motion();
    bool gas();

    // Outputs
    void lightOn();
    void lightOff();
    void openDoor();
    void closeDoor();
    void fanOn();
    void fanOff();
    void fanSpeed(uint8_t speed);

    // Display
    void show(String l1, String l2 = "");
    void showTempHum();  
};
