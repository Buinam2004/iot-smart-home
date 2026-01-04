#include "dht_sensor.h"
#include "pins.h"
#include <DHT.h>

static DHT dht(DHT_PIN, DHT22);
static float lastTemp = 0;
static float lastHum = 0;
static bool dataValid = false;

void DHT_init() { 
    dht.begin(); 
    delay(2000);  // DHT22 cần 2 giây để khởi động
}

bool DHT_read() {
    float t = dht.readTemperature();
    float h = dht.readHumidity();
    
    if (!isnan(t) && !isnan(h)) {
        lastTemp = t;
        lastHum = h;
        dataValid = true;
        return true;
    }
    
    dataValid = false;
    return false;
}

float DHT_getTemperature() { 
    return lastTemp;
}

float DHT_getHumidity() { 
    return lastHum;
}

bool DHT_isValid() {
    return dataValid;
}
