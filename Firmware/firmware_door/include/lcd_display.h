#pragma once
#include <Arduino.h>

// Init LCD
bool LCD_init();

// Trạng thái hiển thị
void LCD_showReady();
void LCD_showScanning();
void LCD_showChecking();
void LCD_showWelcome();
void LCD_showDenied();
void LCD_showGasAlert();
void LCD_showTempHumidity(float temp, float hum);
