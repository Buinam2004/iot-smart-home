#pragma once
#include <Arduino.h>

// Fan điều khiển bằng module L9110H
// INA=HIGH, INB=LOW  → Quay thuận (ON)
// INA=LOW,  INB=LOW  → Dừng (OFF)
// INA=LOW,  INB=HIGH → Quay ngược
// INA=HIGH, INB=HIGH → Brake

void Fan_init();
void Fan_on();
void Fan_off();
void Fan_toggle();
void Fan_setState(bool state);
bool Fan_getState();
