#pragma once

// ============== CẤU HÌNH CHÂN GPIO - ESP8266 NodeMCU ==============
// Dựa trên sơ đồ chân ESP8266 12-E NodeMCU
// Tránh: GPIO0, GPIO2, GPIO15 (ảnh hưởng boot) - dùng cẩn thận cho output
// Tránh: GPIO6-11 (dành cho Flash)
// GPIO16: không hỗ trợ interrupt, PWM hạn chế

// ===== I2C LCD (Chân chuẩn I2C) =====
#define I2C_SDA         D2      // GPIO4 - I2C SDA chuẩn
#define I2C_SCL         D1      // GPIO5 - I2C SCL chuẩn

// ===== CẢM BIẾN =====
#define MQ2_PIN         A0      // ADC0 - Analog duy nhất (cảm biến gas)
#define DHT_PIN         D5      // GPIO14 - DHT22 (nhiệt độ/độ ẩm)
#define PIR_PIN         D6      // GPIO12 - PIR sensor (chuyển động)

// ===== ĐÈN LED =====
#define LED_PIR_PIN     D7      // GPIO13 - LED cho PIR (bật khi có người)
#define LED_GAS_PIN     D0      // GPIO16 - LED cho MQ2 (nhấp nháy khi gas alert)

// ===== QUẠT - Module L9110H =====
#define FAN_INA_PIN     D3      // GPIO0 - L9110H INA (cẩn thận: phải HIGH khi boot)
#define FAN_INB_PIN     D4      // GPIO2 - L9110H INB (cẩn thận: phải HIGH khi boot)

// ===== NÚT BẤM =====
#define BTN_FAN_PIN     D8      // GPIO15 - Button quạt (có pull-down)

// ===== NGƯỠNG & TIMING =====
#define GAS_THRESHOLD       400     // Ngưỡng cảnh báo gas (điều chỉnh theo thực tế)
#define PIR_TIMEOUT_MS      10000   // 10 giây tắt đèn sau khi không có chuyển động
#define DHT_READ_INTERVAL   5000    // 5 giây đọc DHT
#define MQ2_READ_INTERVAL   1000    // 1 giây đọc MQ2
#define LCD_UPDATE_INTERVAL 1000    // 1 giây cập nhật LCD
#define BLINK_INTERVAL      250     // 250ms nhấp nháy khi gas alert
#define DEBOUNCE_DELAY      50      // 50ms debounce cho button


