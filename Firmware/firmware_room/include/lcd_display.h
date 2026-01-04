#pragma once
#include <Arduino.h>

// Chế độ hiển thị
enum DisplayMode {
    DISPLAY_NORMAL,         // Hiển thị nhiệt độ, độ ẩm
    DISPLAY_GAS_ALERT,      // Hiển thị cảnh báo gas
    DISPLAY_INFO            // Hiển thị thông tin khác
};

bool LCD_init();
void LCD_clear();
void LCD_setBacklight(bool on);
void LCD_print(const char* msg, uint8_t row = 0);

// Các chế độ hiển thị
void LCD_showNormal(float temp, float humidity, bool fanOn, bool lightOn);
void LCD_showGasAlert(int gasValue);
void LCD_showMotion(bool detected);
void LCD_showInfo(const char* line1, const char* line2);
void LCD_showStartup();

void LCD_setMode(DisplayMode newMode);
DisplayMode LCD_getMode();
bool LCD_isInitialized();
