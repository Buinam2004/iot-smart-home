#pragma once
#include <Arduino.h>

void Button_init();
bool Button_fanPressed();           // Kiểm tra button quạt có được nhấn
bool Button_fanReleased();          // Kiểm tra button quạt vừa nhả ra (cho toggle)
void Button_update();               // Gọi trong loop() để cập nhật trạng thái
