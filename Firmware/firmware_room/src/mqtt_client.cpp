#include "mqtt_client.h"

#include <ESP8266WiFi.h>
#include <WiFiClientSecureBearSSL.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

#include "smarthome_room.h"

// ===================================================
// EMQX CLOUD CONFIG
// ===================================================
#define MQTT_HOST "sdf8e281.ala.asia-southeast1.emqxsl.com"
#define MQTT_PORT 8883

#define MQTT_USER "firmware_room"
#define MQTT_PASS "12345678"

// ===================================================
// CA CERTIFICATE (EMQX Cloud CA)
// ===================================================
static const char caCert[] PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh
MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3
d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH
MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT
MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j
b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG
9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI
2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx
1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ
q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz
tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ
vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP
BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV
5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY
1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4
NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG
Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91
8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe
pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl
MrY=
-----END CERTIFICATE-----
)EOF";


// ===================================================
// MQTT OBJECTS (ESP8266 + BearSSL)
// ===================================================
static BearSSL::WiFiClientSecure net;
static PubSubClient mqtt(net);

// Trust anchor
static BearSSL::X509List cert(caCert);

// ===================================================
// MQTT CALLBACK (RECEIVE FROM BACKEND)
// ===================================================
static void mqttCallback(char* topic, byte* payload, unsigned int length) {
    String t(topic);
    String msg;

    for (unsigned int i = 0; i < length; i++) {
        msg += (char)payload[i];
    }

    Serial.printf("[MQTT] RX %s -> %s\n", t.c_str(), msg.c_str());

    // ===== PARSE JSON =====
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, msg);
    
    if (error) {
        Serial.print("[MQTT] JSON parse error: ");
        Serial.println(error.c_str());
        return;
    }

    // ===== XỬ LÝ COMMAND DỰA TRÊN TYPE =====
    const char* type = doc["type"];
    
    if (strcmp(type, "command") == 0) {
        const char* device = doc["device"];
        
        // ----- FAN CONTROL -----
        if (strcmp(device, "fan") == 0) {
            int state = doc["state"];
            Room_setFanState(state == 1);
        }
        
        // ----- GAS ALERT CLEAR -----
        else if (strcmp(device, "gas") == 0) {
            const char* action = doc["action"];
            if (strcmp(action, "clear") == 0) {
                Room_clearGasAlert();
            }
        }
        
        // ----- PIR LED CONTROL -----
        else if (strcmp(device, "led_pir") == 0) {
            int state = doc["state"];
            Room_setPIRLedState(state == 1);
        }
    }
}

// ===================================================
// INIT MQTT
// ===================================================
void MQTT_init() {
    // Set CA cert (ESP8266 BearSSL)
    net.setTrustAnchors(&cert);

    // Reduce RAM pressure
    net.setBufferSizes(512, 512);

    mqtt.setServer(MQTT_HOST, MQTT_PORT);
    mqtt.setCallback(mqttCallback);

    Serial.println("[MQTT] Init done");
}

// ===================================================
// MQTT LOOP (CALL IN loop())
// ===================================================
void MQTT_loop() {
    if (!mqtt.connected()) {
        String clientId = "esp8266_room_";
        clientId += ESP.getChipId();

        Serial.print("[MQTT] Connecting... ");

        if (mqtt.connect(clientId.c_str(), MQTT_USER, MQTT_PASS)) {
            Serial.println("OK");

            // Subscribe single topic
            mqtt.subscribe("iot_smarthome/room1");
            Serial.println("[MQTT] Subscribed: iot_smarthome/room1");
        } else {
            Serial.print("FAILED, rc=");
            Serial.println(mqtt.state());
            delay(3000);
            return;
        }
    }

    mqtt.loop();
}

// ===================================================
// PUBLISH (USED BY Room CALLBACK)
// ===================================================
void MQTT_publish(const char* topic, const char* payload) {
    if (!mqtt.connected()) return;
    mqtt.publish(topic, payload);
}
