#include "servo_door.h"
#include "pins.h"
#include <Servo.h>

Servo door;

void Door_init() { door.attach(SERVO_PIN); }
void Door_open() { door.write(90); }
void Door_close() { door.write(0); }
