#include "lcd_display.h"
#include "pins.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

LiquidCrystal_I2C lcd(0x27, 16, 2);

void LCD_init() {
    Wire.begin(I2C_SDA, I2C_SCL);
    lcd.init();
    lcd.backlight();
    lcd.clear();
}

void LCD_clear() {
    lcd.clear();
}

void LCD_print(String msg, uint8_t row) {
    lcd.setCursor(0, row);
    lcd.print("                "); 
    lcd.setCursor(0, row);
    lcd.print(msg);
}

void LCD_showTempHum(float temp, float hum) {
    lcd.setCursor(0, 0);
    lcd.print("Temp:");
    lcd.print(temp, 1);
    lcd.print("C   ");

    lcd.setCursor(0, 1);
    lcd.print("Hum :");
    lcd.print(hum, 1);
    lcd.print("%   ");
}