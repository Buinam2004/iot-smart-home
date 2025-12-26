#include "servo_door.h"
#include <Servo.h>
#include <pins.h>


#define SERVO_OPEN_ANGLE  30
#define SERVO_CLOSE_ANGLE 0

static Servo doorServo;

void Door_init() {
    doorServo.attach(SERVO_PIN);
    doorServo.write(SERVO_CLOSE_ANGLE);
}

void Door_open() {
    doorServo.write(SERVO_OPEN_ANGLE);
}

void Door_close() {
    doorServo.write(SERVO_CLOSE_ANGLE);
}
