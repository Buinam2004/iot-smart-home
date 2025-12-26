#pragma once
#include <Arduino.h>

// Init LCD
bool LCD_init();

// Trạng thái hiển thị
void LCD_showReady();
void LCD_showChecking();
void LCD_showWelcome();
void LCD_showDenied();
void LCD_showGasAlert();
