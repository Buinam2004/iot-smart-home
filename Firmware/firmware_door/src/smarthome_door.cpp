#include "smarthome_door.h"

#include "rfid_reader.h"
#include "servo_door.h"
#include "lcd_display.h"

static DoorDataCallback dataCallback = nullptr;

static bool doorOpened = false;
static unsigned long doorOpenTime = 0;

#define DOOR_AUTO_CLOSE_MS 7000

// ===================================================
void DoorController_init() {
    Serial.println("[DOOR] Init start");

    RFID_init();
    Serial.println("[DOOR] RFID init OK");

    Door_init();                 // ✅ GIỮ NGUYÊN API CŨ
    Serial.println("[DOOR] Servo init OK");

    LCD_init();
    LCD_showReady();
    Serial.println("[DOOR] LCD ready");
}

// ===================================================
void DoorController_update() {

    // ===== AUTO CLOSE =====
    if (doorOpened && millis() - doorOpenTime > DOOR_AUTO_CLOSE_MS) {
        Door_close();            // ✅ GIỮ NGUYÊN API CŨ
        doorOpened = false;
        LCD_showReady();
        Serial.println("[DOOR] Auto close");
    }

    // ===== RFID SCAN =====
    if (RFID_read()) {
        String uid = RFID_getUID();
        Serial.print("[DOOR] RFID scanned: ");
        Serial.println(uid);

        LCD_showChecking();

        if (dataCallback) {
            char payload[160];
            snprintf(payload, sizeof(payload),
                "{"
                  "\"device_id\":\"door_esp_01\","
                  "\"entity_id\":\"rfid_01\","
                  "\"event\":\"SCAN\","
                  "\"uid\":\"%s\""
                "}",
                uid.c_str()
            );

            Serial.println("[DOOR] Calling MQTT callback...");
            dataCallback("iot_smarthome/door1/event", payload);
            Serial.println("[DOOR] MQTT callback called");
        } else {
            Serial.println("[DOOR] ERROR: dataCallback is NULL");
        }
    }
}

// ===================================================
void Door_handleCommand(const char* payload) {
    Serial.print("[DOOR] MQTT RX: ");
    Serial.println(payload);

    // ===== GAS ALERT FROM KIT 1 =====
    if (strstr(payload, "\"alert\":1")) {
        LCD_showGasAlert();
        Serial.println("[DOOR] GAS ALERT shown");
        return;
    }

    // ===== OPEN =====
    if (strstr(payload, "\"command\":\"OPEN\"")) {
        Door_open();             // ✅ GIỮ NGUYÊN API CŨ
        doorOpened = true;
        doorOpenTime = millis();
        LCD_showWelcome();
        Serial.println("[DOOR] OPEN executed");
        return;
    }

    // ===== DENY =====
    if (strstr(payload, "\"command\":\"DENY\"")) {
        LCD_showDenied();
        Serial.println("[DOOR] DENY shown");
        return;
    }
}

// ===================================================
void Door_setDataCallback(DoorDataCallback cb) {
    dataCallback = cb;
    Serial.println("[DOOR] DataCallback set");
}
void ControlDoor_init() {
    DoorController_init();
}

void ControlDoor_update() {
    DoorController_update();
}
