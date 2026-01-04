#include "lcd_display.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ================= CONFIG =================
#define LCD_ADDR 0x27
#define LCD_COLS 16
#define LCD_ROWS 2
// ==========================================

static LiquidCrystal_I2C lcd(LCD_ADDR, LCD_COLS, LCD_ROWS);

bool LCD_init() {
    Wire.begin();
    lcd.init();
    lcd.backlight();
    LCD_showReady();
    return true;
}

void LCD_showReady() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("DOOR READY");
    lcd.setCursor(0, 1);
    lcd.print("SCAN CARD");
}

void LCD_showScanning() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("CARD DETECTED");
    lcd.setCursor(0, 1);
    lcd.print("SCANNING...");
}

void LCD_showChecking() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("CHECKING...");
    lcd.setCursor(0, 1);
    lcd.print("PLEASE WAIT");
}

void LCD_showWelcome() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WELCOME");
    lcd.setCursor(0, 1);
    lcd.print("DOOR OPEN");
}

void LCD_showDenied() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("ACCESS DENIED");
}

void LCD_showGasAlert() {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("!!! GAS ALERT !!!");
    lcd.setCursor(0, 1);
    lcd.print("CHECK IMMEDIATELY");
}

void LCD_showTempHumidity(float temp, float hum) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Temp: ");
    lcd.print(temp, 1);
    lcd.print((char)223);  // Ký tự độ
    lcd.print("C");
    lcd.setCursor(0, 1);
    lcd.print("Hum:  ");
    lcd.print(hum, 1);
    lcd.print("%");
}
