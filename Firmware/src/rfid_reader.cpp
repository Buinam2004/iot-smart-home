#include "rfid_reader.h"
#include "pins.h"
#include <SPI.h>
#include <MFRC522.h>

MFRC522 rfid(RFID_SDA, RFID_RST);

void RFID_init() {
    SPI.begin();  
    rfid.PCD_Init();
}

String RFID_read() {
    if (!rfid.PICC_IsNewCardPresent()) return "";
    if (!rfid.PICC_ReadCardSerial()) return "";

    String uid = "";
    for (byte i = 0; i < rfid.uid.size; i++)
        uid += String(rfid.uid.uidByte[i], HEX);

    rfid.PICC_HaltA();
    return uid;
}
