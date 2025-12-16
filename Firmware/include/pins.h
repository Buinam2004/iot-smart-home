#pragma once

// I2C bus (LCD + Keypad I2C)
#define I2C_SDA D2
#define I2C_SCL D1

// Servo SG90 - cửa
#define SERVO_PIN D4

// PIR
#define PIR_PIN D8

// DHT22
#define DHT_PIN D3

// MQ-2 Gas
#define MQ2_PIN A0

// Relay (đèn)
#define RELAY_PIN D0

// L9110H fan control (PWM pins)
#define FAN_IN1 D5
#define FAN_IN2 D6

// RFID RC522 SPI (hardware SPI pins cho ESP8266)
#define RFID_SDA D8  // SS/CS pin
#define RFID_RST D4  // Reset pin
