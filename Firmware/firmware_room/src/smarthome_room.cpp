#include "smarthome_room.h"
#include "pins.h"
#include "pir_sensor.h"
#include "dht_sensor.h"
#include "mq2_sensor.h"
#include "light_control.h"
#include "fan_control.h"
#include "button.h"
#include "lcd_display.h"
#include <ArduinoJson.h>
#include <time.h>

// ===== HELPER: Lấy timestamp =====
static void getTimestamp(char* buffer, size_t size) {
    time_t now = time(nullptr);
    struct tm* timeinfo = localtime(&now);
    strftime(buffer, size, "%Y-%m-%d %H:%M:%S", timeinfo);
}

// ===== STATE =====
static SystemState systemState = STATE_NORMAL;
static bool pirLedOn = false;

// ===== TIMING =====
static unsigned long lastDHTRead = 0;
static unsigned long lastMQ2Read = 0;
static unsigned long lastLCDUpdate = 0;

// ===== SENSOR DATA =====
static double temperature = 0;
static double humidity = 0;
static int gasValue = 0;

// ===== MQTT CALLBACK =====
static DataCallback dataCallback = nullptr;

// ===== PRIVATE =====
static void handleNormalState();
static void handleGasAlertState();
static void handleButton();
static void handlePIR();
static void updateLCD();

// ===== INIT =====
void Room_init() {
    LCD_init();
    LCD_showStartup();

    DHT_init();
    MQ2_init();
    PIR_init();

    LedPIR_init();
    LedGAS_init();
    Fan_init();
    Button_init();

    delay(2000);
}

// ===== LOOP =====
void Room_update() {
    unsigned long now = millis();

    Button_update();
    LedGAS_update();

    // ===== MQ2 luôn đọc =====
    if (now - lastMQ2Read >= MQ2_READ_INTERVAL) {
        lastMQ2Read = now;
        gasValue = MQ2_read();

        if (MQ2_isAlertActive() && systemState != STATE_GAS_ALERT) {
            systemState = STATE_GAS_ALERT;
            LedGAS_startBlinking(BLINK_INTERVAL);

            if (dataCallback) {
                char timeStr[20];
                getTimestamp(timeStr, sizeof(timeStr));
                
                StaticJsonDocument<256> doc;
                doc["type"] = "gas";
                doc["event"] = "alert";
                doc["value"] = gasValue;
                doc["state"] = 1;
                doc["timestamp"] = timeStr;
                
                String payload;
                serializeJson(doc, payload);
                dataCallback("iot_smarthome/room1", payload.c_str());
            }
        }
    }

    if (systemState == STATE_GAS_ALERT) {
        handleGasAlertState();
    } else {
        handleNormalState();
    }

    if (now - lastLCDUpdate >= LCD_UPDATE_INTERVAL) {
        lastLCDUpdate = now;
        updateLCD();
    }
}

// ===== NORMAL =====
static void handleNormalState() {
    unsigned long now = millis();

    if (now - lastDHTRead >= DHT_READ_INTERVAL) {
        lastDHTRead = now;
        if (DHT_read()) {
            temperature = DHT_getTemperature();
            humidity = DHT_getHumidity();

            if (dataCallback) {
                char timeStr[20];
                getTimestamp(timeStr, sizeof(timeStr));
                
                StaticJsonDocument<256> doc;
                doc["type"] = "sensor";
                doc["sensor"] = "dht";
                doc["temperature"] = temperature;
                doc["humidity"] = humidity;
                doc["timestamp"] = timeStr;
                
                String payload;
                serializeJson(doc, payload);
                dataCallback("iot_smarthome/room1", payload.c_str());
            }
        }
    }

    handleButton();
    handlePIR();
}

// ===== GAS ALERT =====
static void handleGasAlertState() {
    if (Button_fanReleased()) {
        Fan_toggle();
        if (dataCallback) {
            char timeStr[20];
            getTimestamp(timeStr, sizeof(timeStr));
            
            StaticJsonDocument<256> doc;
            doc["type"] = "device";
            doc["device"] = "fan";
            doc["state"] = Fan_getState() ? 1 : 0;
            doc["timestamp"] = timeStr;
            
            String payload;
            serializeJson(doc, payload);
            dataCallback("iot_smarthome/room1", payload.c_str());
        }
    }
}

// ===== BUTTON FAN =====
static void handleButton() {
    if (Button_fanReleased()) {
        Fan_toggle();
        Serial.printf("[BUTTON] Fan toggled: %s\n", Fan_getState() ? "ON" : "OFF");
        
        if (dataCallback) {
            char timeStr[20];
            getTimestamp(timeStr, sizeof(timeStr));
            
            StaticJsonDocument<256> doc;
            doc["type"] = "device";
            doc["device"] = "fan";
            doc["state"] = Fan_getState() ? 1 : 0;
            doc["timestamp"] = timeStr;
            
            String payload;
            serializeJson(doc, payload);
            dataCallback("iot_smarthome/room1", payload.c_str());
        }
    }
}

// ===== PIR =====
static void handlePIR() {
    PIR_read();

    // Nếu đèn đang tắt VÀ có chuyển động → bật đèn
    if (!pirLedOn && PIR_isMotionDetected()) {
        LedPIR_on();
        pirLedOn = true;
        Serial.println("[PIR] Motion detected! LED ON");

        if (dataCallback) {
            char timeStr[20];
            getTimestamp(timeStr, sizeof(timeStr));
            
            StaticJsonDocument<256> doc;
            doc["type"] = "sensor";
            doc["sensor"] = "pir";
            doc["motion"] = 1;
            doc["light"] = 1;
            doc["timestamp"] = timeStr;
            
            String payload;
            serializeJson(doc, payload);
            dataCallback("iot_smarthome/room1", payload.c_str());
        }
    }
    
    // Nếu đèn đang bật VÀ có chuyển động → reset timer (giữ đèn sáng)
    if (pirLedOn && PIR_isMotionDetected()) {
        PIR_resetTimer();
    }

    // Nếu đèn đang bật VÀ đã hết timeout → tắt đèn
    if (pirLedOn && PIR_shouldTurnOffLight()) {
        LedPIR_off();
        pirLedOn = false;
        Serial.println("[PIR] No motion for 10s. LED OFF");

        if (dataCallback) {
            char timeStr[20];
            getTimestamp(timeStr, sizeof(timeStr));
            
            StaticJsonDocument<256> doc;
            doc["type"] = "sensor";
            doc["sensor"] = "pir";
            doc["motion"] = 0;
            doc["light"] = 0;
            doc["timestamp"] = timeStr;
            
            String payload;
            serializeJson(doc, payload);
            dataCallback("iot_smarthome/room1", payload.c_str());
        }
    }
}

// ===== LCD =====
static void updateLCD() {
    if (systemState == STATE_GAS_ALERT) {
        LCD_showGasAlert(gasValue);
    } else {
        LCD_showNormal(temperature, humidity, Fan_getState(), pirLedOn);
    }
}

// ===== MQTT API =====
void Room_setDataCallback(DataCallback callback) {
    dataCallback = callback;
}

void Room_setFanState(bool on) {
    Fan_setState(on);
    if (dataCallback) {
        char timeStr[20];
        getTimestamp(timeStr, sizeof(timeStr));
        
        StaticJsonDocument<256> doc;
        doc["type"] = "device";
        doc["device"] = "fan";
        doc["state"] = on ? 1 : 0;
        doc["timestamp"] = timeStr;
        
        String payload;
        serializeJson(doc, payload);
        dataCallback("iot_smarthome/room1", payload.c_str());
    }
}

void Room_clearGasAlert() {
    if (systemState == STATE_GAS_ALERT) {
        MQ2_deactivateAlert();
        systemState = STATE_NORMAL;
        LedGAS_stopBlinking();

        if (dataCallback) {
            char timeStr[20];
            getTimestamp(timeStr, sizeof(timeStr));
            
            StaticJsonDocument<256> doc;
            doc["type"] = "gas";
            doc["event"] = "clear";
            doc["state"] = 0;
            doc["timestamp"] = timeStr;
            
            String payload;
            serializeJson(doc, payload);
            dataCallback("iot_smarthome/room1", payload.c_str());
        }
    }
}

void Room_setPIRLedState(bool on) {
    if (on) {
        LedPIR_on();
        pirLedOn = true;
        PIR_resetTimer();  // Reset timer để tránh tự động tắt ngay
    } else {
        LedPIR_off();
        pirLedOn = false;
    }
    
    if (dataCallback) {
        char timeStr[20];
        getTimestamp(timeStr, sizeof(timeStr));
        
        StaticJsonDocument<256> doc;
        doc["type"] = "device";
        doc["device"] = "led_pir";
        doc["state"] = on ? 1 : 0;
        doc["timestamp"] = timeStr;
        
        String payload;
        serializeJson(doc, payload);
        dataCallback("iot_smarthome/room1", payload.c_str());
    }
}

// ===== GETTERS =====
double Room_getTemperature() { return temperature; }
double Room_getHumidity()    { return humidity; }
int   Room_getGasValue()    { return gasValue; }
bool  Room_isFanOn()        { return Fan_getState(); }
bool  Room_isLightOn()      { return pirLedOn; }
bool  Room_isGasAlert()     { return systemState == STATE_GAS_ALERT; }
bool  Room_isMotionDetected() { return PIR_isMotionDetected(); }
SystemState Room_getState() { return systemState; }
