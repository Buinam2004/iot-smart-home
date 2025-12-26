#include "rfid_reader.h"
#include "pins.h"

#include <SPI.h>
#include <MFRC522.h>

static MFRC522 mfrc522(RFID_SDA, RFID_RST);

static String lastUID = "";

// ===================================================
void RFID_init() {
    // Init SPI với pin đã định nghĩa
    SPI.begin();
    mfrc522.PCD_Init();
}

// ===================================================
bool RFID_read() {
    if (!mfrc522.PICC_IsNewCardPresent()) return false;
    if (!mfrc522.PICC_ReadCardSerial()) return false;

    lastUID = "";
    for (byte i = 0; i < mfrc522.uid.size; i++) {
        if (mfrc522.uid.uidByte[i] < 0x10) {
            lastUID += "0";
        }
        lastUID += String(mfrc522.uid.uidByte[i], HEX);
    }

    lastUID.toUpperCase();

    // Stop RFID
    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();

    return true;
}

// ===================================================
String RFID_getUID() {
    return lastUID;
}
