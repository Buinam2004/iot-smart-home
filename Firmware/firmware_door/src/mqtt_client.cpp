#include "mqtt_client.h"

#include <ESP8266WiFi.h>
#include <WiFiClientSecureBearSSL.h>
#include <PubSubClient.h>

#include "smarthome_door.h"

#define MQTT_HOST "sdf8e281.ala.asia-southeast1.emqxsl.com"
#define MQTT_PORT 8883

#define MQTT_USER "firmware_door"
#define MQTT_PASS "12345678"

// ===== CA CERT (GIỐNG KIT 1) =====
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
static BearSSL::WiFiClientSecure net;
static PubSubClient mqtt(net);
static BearSSL::X509List cert(caCert);

static void mqttCallback(char* topic, byte* payload, unsigned int length) {
    char msg[256];
    memcpy(msg, payload, length);
    msg[length] = '\0';

    Serial.printf("[MQTT] RX %s <- %s\n", topic, msg);

    // Từ room1 (gas alert)
    if (strcmp(topic, "iot_smarthome/room1") == 0) {
        Door_handleCommand(msg);
        return;
    }

    // Từ door1 (command từ backend)
    if (strcmp(topic, "iot_smarthome/door1") == 0) {
        Door_handleCommand(msg);
        return;
    }
}

void MQTT_init() {
    net.setTrustAnchors(&cert);
    net.setBufferSizes(512, 512);

    mqtt.setServer(MQTT_HOST, MQTT_PORT);
    mqtt.setCallback(mqttCallback);
}


void MQTT_loop() {
    if (!mqtt.connected()) {
        static unsigned long lastTry = 0;
        if (millis() - lastTry > 3000) {
            lastTry = millis();

            Serial.println("[MQTT] Attempting connection...");
            String cid = "esp8266_door_" + String(ESP.getChipId());
            if (mqtt.connect(cid.c_str(), MQTT_USER, MQTT_PASS)) {
                Serial.println("[MQTT] Connected");

                // Subscribe topic chính
                mqtt.subscribe("iot_smarthome/door1");
                mqtt.subscribe("iot_smarthome/room1");
                Serial.println("[MQTT] Subscribed to door1 & room1");
            } else {
                Serial.print("[MQTT] Connection failed, state: ");
                Serial.println(mqtt.state());
                // States: -4=timeout, -3=lost, -2=failed, -1=disconnected, 0=connected
                //         1=bad protocol, 2=bad client id, 3=unavailable, 4=bad credentials, 5=unauthorized
            }
        }
        return;
    }
    mqtt.loop();
}


void MQTT_publish(const char* topic, const char* payload) {
    if (!mqtt.connected()) {
        // Thử kết nối lại ngay khi cần publish
        static unsigned long lastTry = 0;
        if (millis() - lastTry > 1000) {
            lastTry = millis();
            String cid = "esp8266_door_" + String(ESP.getChipId());
            if (mqtt.connect(cid.c_str(), MQTT_USER, MQTT_PASS)) {
                mqtt.subscribe("iot_smarthome/door1");
                mqtt.subscribe("iot_smarthome/room1");
                Serial.println("[MQTT] Reconnected for publish");
            } else {
                Serial.println("[MQTT] ERROR: Not connected, cannot publish");
                return;
            }
        } else {
            Serial.println("[MQTT] ERROR: Not connected, cannot publish");
            return;
        }
    }
    Serial.printf("[MQTT] TX %s -> %s\n", topic, payload);
    mqtt.publish(topic, payload);
}
