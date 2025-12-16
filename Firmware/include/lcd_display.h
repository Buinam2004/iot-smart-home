#pragma once
#include <Arduino.h>

void LCD_init();
void LCD_clear();
void LCD_print(String msg, uint8_t row = 0);
void LCD_showTempHum(float temp, float hum);