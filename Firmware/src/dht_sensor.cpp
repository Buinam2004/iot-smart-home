#include "dht_sensor.h"
#include "pins.h"
#include <DHT.h>

DHT dht(DHT_PIN, DHT22);

void DHT_init() { dht.begin(); }
float getTemperature() { return dht.readTemperature(); }
float getHumidity() { return dht.readHumidity(); }
