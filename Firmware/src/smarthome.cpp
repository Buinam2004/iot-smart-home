#include "smarthome.h"

#include "lcd_display.h"
#include "keypad_i2c.h"
#include "rfid_reader.h"
#include "dht_sensor.h"
#include "pir_sensor.h"
#include "mq2_sensor.h"
#include "light_control.h"
#include "servo_door.h"
#include "fan_control.h"


// INIT
void SmartHome::begin() {
    LCD_init();
    Keypad_init();
    RFID_init();
    DHT_init();
    PIR_init();
    Light_init();
    Door_init();
    Fan_init();

    LCD_print("System Ready", 0);
}

// INPUT FUNCTIONS
String SmartHome::readRFID() { 
    return RFID_read(); 
}

char SmartHome::readKey() { 
    return Keypad_getKey(); 
}

float SmartHome::getTemp() { 
    return getTemperature(); 
}

float SmartHome::getHum() { 
    return getHumidity(); 
}

bool SmartHome::motion() { 
    return PIR_detected(); 
}

bool SmartHome::gas() {
    return MQ2_gasDetected();
}


// OUTPUT FUNCTIONS
void SmartHome::lightOn() { 
    Light_on(); 
}

void SmartHome::lightOff() { 
    Light_off(); 
}

void SmartHome::openDoor() { 
    Door_open(); 
}

void SmartHome::closeDoor() { 
    Door_close(); 
}

void SmartHome::fanOn() { 
    Fan_on(); 
}

void SmartHome::fanOff() { 
    Fan_off(); 
}

void SmartHome::fanSpeed(uint8_t s) { 
    Fan_speed(s); 
}

//-----------------------------------------
// LCD DISPLAY
//-----------------------------------------
void SmartHome::show(String l1, String l2) {
    LCD_print(l1, 0);
    if (l2 != "") LCD_print(l2, 1);
}

void SmartHome::showTempHum() {
    float t = getTemperature();
    float h = getHumidity();
    LCD_showTempHum(t, h);
}
