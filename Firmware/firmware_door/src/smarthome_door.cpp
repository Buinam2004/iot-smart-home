#include "smarthome_door.h"

#include "rfid_reader.h"
#include "servo_door.h"
#include "lcd_display.h"

static DoorDataCallback dataCallback = nullptr;

static bool doorOpened = false;
static unsigned long doorOpenTime = 0;

#define DOOR_AUTO_CLOSE_MS 7000

// ===================================================
void ControlDoor_init() {
    Serial.println("[DOOR] Init start");

    RFID_init();
    Serial.println("[DOOR] RFID init OK");

    Door_init();                 // GIỮ NGUYÊN API CŨ
    Serial.println("[DOOR] Servo init OK");

    LCD_init();
    LCD_showReady();
    Serial.println("[DOOR] LCD ready");
}

// ===================================================
void ControlDoor_update() {

    // ===== AUTO CLOSE =====
    if (doorOpened && millis() - doorOpenTime > DOOR_AUTO_CLOSE_MS) {
        Door_close();            // GIỮ NGUYÊN API CŨ
        doorOpened = false;
        LCD_showReady();
        Serial.println("[DOOR] Auto close");
    }

    // ===== RFID SCAN =====
    if (RFID_read()) {
        String uid = RFID_getUID();
        Serial.print("[DOOR] RFID scanned: ");
        Serial.println(uid);

        LCD_showScanning();
        delay(500);  // Hiển thị scanning 0.5s
        LCD_showChecking();

        if (dataCallback) {
            // Lấy thời gian thực
            time_t now = time(nullptr);
            struct tm* timeinfo = localtime(&now);
            char timeStr[20];
            strftime(timeStr, sizeof(timeStr), "%Y-%m-%d %H:%M:%S", timeinfo);

            char payload[200];
            snprintf(payload, sizeof(payload),
                "{"
                  "\"type\":\"rfid\","
                  "\"event\":\"scan\","
                  "\"uid\":\"%s\","
                  "\"timestamp\":\"%s\""
                "}",
                uid.c_str(),
                timeStr
            );

            Serial.println("[DOOR] Calling MQTT callback...");
            dataCallback("iot_smarthome/door1", payload);
            Serial.println("[DOOR] MQTT callback called");
        } else {
            Serial.println("[DOOR] ERROR: dataCallback is NULL");
        }
    }
}

// ===================================================
void Door_handleCommand(const char* payload) {
    Serial.print("[DOOR] Processing: ");
    Serial.println(payload);

    // ===== DHT SENSOR DATA FROM ROOM1 =====
    // Format: {"type":"sensor","sensor":"dht","temperature":21.1,"humidity":75.4}
    if (strstr(payload, "\"type\"") && strstr(payload, "\"sensor\"") && strstr(payload, "\"dht\"")) {
        // Parse temperature
        const char* tempPtr = strstr(payload, "\"temperature\"");
        const char* humPtr = strstr(payload, "\"humidity\"");
        
        if (tempPtr && humPtr) {
            // Tìm số sau dấu :
            tempPtr = strchr(tempPtr, ':');
            humPtr = strchr(humPtr, ':');
            if (tempPtr && humPtr) {
                double temp = atof(tempPtr + 1);
                double hum = atof(humPtr + 1);
                
                LCD_showTempHumidity(temp, hum);
                Serial.printf("[DOOR] DHT: %.1fC, %.1f%%\n", temp, hum);
            }
        }
        return;
    }

    // ===== GAS ALERT FROM ROOM1 =====
    // Format: {"type":"gas","event":"alert","value":517,"state":1}
    if (strstr(payload, "\"type\"") && strstr(payload, "\"gas\"") && strstr(payload, "\"alert\"") && strstr(payload, "\"state\"")) {
        if (strstr(payload, ":1") || strstr(payload, ": 1")) {
            LCD_showGasAlert();
            Serial.println("[DOOR] GAS ALERT ON");
        }
        return;
    }

    // ===== GAS CLEAR COMMAND FROM BACKEND =====
    // Format: {"type":"command","device":"gas","action":"clear"}
    if (strstr(payload, "\"command\"") && strstr(payload, "\"gas\"") && strstr(payload, "\"clear\"")) {
        LCD_showTempHumidity(0, 0);  // Tạm về màn hình mặc định, sẽ update khi có data DHT mới
        Serial.println("[DOOR] GAS CLEAR - back to normal");
        return;
    }

    // ===== DOOR OPEN COMMAND =====
    // Format: {"type":"command","device":"door","action":"open"}
    if (strstr(payload, "\"command\"") && strstr(payload, "\"door\"") && strstr(payload, "\"open\"")) {
        Door_open();
        doorOpened = true;
        doorOpenTime = millis();
        LCD_showWelcome();
        Serial.println("[DOOR] OPEN executed");
        return;
    }

    // ===== DOOR DENY COMMAND =====
    // Format: {"type":"command","device":"door","action":"deny"}
    if (strstr(payload, "\"command\"") && strstr(payload, "\"door\"") && strstr(payload, "\"deny\"")) {
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
