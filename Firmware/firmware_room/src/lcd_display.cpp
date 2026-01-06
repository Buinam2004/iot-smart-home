#include "lcd_display.h"
#include "pins.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

static LiquidCrystal_I2C lcd(0x27, 16, 2);
static bool initialized = false;
static DisplayMode displayMode = DISPLAY_NORMAL;

bool LCD_init() {
    Wire.begin(I2C_SDA, I2C_SCL);
    delay(100);
    
    // Scan I2C để kiểm tra LCD có tồn tại
    Wire.beginTransmission(0x27);
    if (Wire.endTransmission() == 0) {
        lcd = LiquidCrystal_I2C(0x27, 16, 2);
        lcd.init();
        lcd.backlight();
        initialized = true;
        return true;
    }
    
    // Thử địa chỉ 0x3F
    Wire.beginTransmission(0x3F);
    if (Wire.endTransmission() == 0) {
        lcd = LiquidCrystal_I2C(0x3F, 16, 2);
        lcd.init();
        lcd.backlight();
        initialized = true;
        return true;
    }
    
    initialized = false;
    return false;
}

void LCD_clear() {
    if (initialized) {
        lcd.clear();
    }
}

void LCD_setBacklight(bool on) {
    if (initialized) {
        if (on) lcd.backlight();
        else lcd.noBacklight();
    }
}

void LCD_print(const char* msg, uint8_t row) {
    if (!initialized) return;
    lcd.setCursor(0, row);
    lcd.print("                ");  // Clear row
    lcd.setCursor(0, row);
    lcd.print(msg);
}

void LCD_showNormal(double temp, double humidity, bool fanOn, bool lightOn) {
    if (!initialized) return;
    
    displayMode = DISPLAY_NORMAL;
    
    // Dòng 1: Nhiệt độ và độ ẩm
    lcd.setCursor(0, 0);
    if (isnan(temp)) {
        lcd.print("T:ERR ");
    } else {
        lcd.print("T:");
        lcd.print(temp, 1);
        lcd.print("C ");
    }
    
    if (isnan(humidity)) {
        lcd.print("H:ERR   ");
    } else {
        lcd.print("H:");
        lcd.print((int)humidity);
        lcd.print("%   ");
    }
    
    // Dòng 2: Trạng thái thiết bị
    lcd.setCursor(0, 1);
    lcd.print("F:");
    lcd.print(fanOn ? "ON " : "OFF");
    lcd.print(" L:");
    lcd.print(lightOn ? "ON     " : "OFF    ");
}

void LCD_showGasAlert(int gasValue) {
    if (!initialized) return;
    
    displayMode = DISPLAY_GAS_ALERT;
    lcd.clear();
    
    // Dòng 1: CẢNH BÁO
    lcd.setCursor(0, 0);
    lcd.print("!! GAS ALERT !!");
    
    // Dòng 2: Giá trị gas
    lcd.setCursor(0, 1);
    lcd.print("Value: ");
    lcd.print(gasValue);
    lcd.print(" !!!");
}

void LCD_showMotion(bool detected) {
    if (!initialized) return;
    
    // Chỉ cập nhật dòng 2
    lcd.setCursor(0, 1);
    if (detected) {
        lcd.print("Motion detected!");
    } else {
        lcd.print("                ");
    }
}

void LCD_showInfo(const char* line1, const char* line2) {
    if (!initialized) return;
    
    displayMode = DISPLAY_INFO;
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(line1);
    lcd.setCursor(0, 1);
    lcd.print(line2);
}

void LCD_showStartup() {
    if (!initialized) return;
    
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Smart Home Room");
    lcd.setCursor(0, 1);
    lcd.print("Initializing...");
}

void LCD_setMode(DisplayMode newMode) {
    displayMode = newMode;
}

DisplayMode LCD_getMode() {
    return displayMode;
}

bool LCD_isInitialized() {
    return initialized;
}
