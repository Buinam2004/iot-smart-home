#include "keypad_i2c.h"
#include "pins.h"
#include <Wire.h>
#include <I2CKeyPad.h>

#define KEYPAD_ADDR 0x20

// Keymap: 4x3 keypad layout
// Character map for key index (row * 4 + col)
char keymap[16] = {
    '1', '2', '3', 'A',
    '4', '5', '6', 'B',
    '7', '8', '9', 'C',
    '*', '0', '#', 'D'
};

I2CKeyPad keypad(KEYPAD_ADDR);

void Keypad_init() {
    Wire.begin(I2C_SDA, I2C_SCL);
    keypad.begin();
    keypad.loadKeyMap(keymap);
}

char Keypad_getKey() {
    char key = keypad.getChar();
    if (key == 'A' || key == 'B' || key == 'C' || key == 'D') {
        return '\0';  // Ignore unused keys for 4x3 keypad
    }
    return (key == I2C_KEYPAD_NOKEY) ? '\0' : key;
}
