#include "mq2_sensor.h"
#include "pins.h"
#include <Arduino.h>

int MQ2_read() { return analogRead(MQ2_PIN); }

bool MQ2_gasDetected(int threshold) {
    return MQ2_read() > threshold;
}
