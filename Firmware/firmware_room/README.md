# ROOM CONTROLLER - ESP8266 Firmware

## Tổng quan

Firmware điều khiển phòng thông minh sử dụng ESP8266 (NodeMCU), tích hợp:

- **DHT22** - Cảm biến nhiệt độ & độ ẩm
- **MQ2** - Cảm biến khí gas
- **PIR HC-SR501** - Cảm biến chuyển động
- **LED PIR** - Đèn tự động bật khi có người
- **LED Gas** - Đèn cảnh báo gas (nhấp nháy)
- **Quạt DC** - Điều khiển qua L9110H
- **LCD I2C 16x2** - Hiển thị trạng thái
- **MQTT over TLS** - Giao tiếp với backend

---

## Cấu trúc thư mục

```
firmware_room/
├── include/
│   ├── pins.h              # Định nghĩa chân GPIO & ngưỡng
│   ├── smarthome_room.h    # API logic điều khiển chính
│   ├── dht_sensor.h        # API cảm biến DHT22
│   ├── mq2_sensor.h        # API cảm biến MQ2
│   ├── pir_sensor.h        # API cảm biến PIR
│   ├── light_control.h     # API điều khiển LED
│   ├── fan_control.h       # API điều khiển quạt
│   ├── button.h            # API nút bấm
│   ├── lcd_display.h       # API màn hình LCD
│   └── mqtt_client.h       # API MQTT client
├── src/
│   ├── main.cpp            # Entry point, WiFi, NTP
│   ├── smarthome_room.cpp  # Logic xử lý chính
│   ├── dht_sensor.cpp      # Đọc DHT22
│   ├── mq2_sensor.cpp      # Đọc MQ2
│   ├── pir_sensor.cpp      # Đọc PIR
│   ├── light_control.cpp   # Điều khiển LED
│   ├── fan_control.cpp     # Điều khiển quạt
│   ├── button.cpp          # Xử lý nút bấm
│   ├── lcd_display.cpp     # Hiển thị LCD
│   └── mqtt_client.cpp     # MQTT client với SSL
├── platformio.ini          # Cấu hình PlatformIO
└── README.md               # File này
```

---

## Sơ đồ chân kết nối (ESP8266 NodeMCU)

### LCD I2C (16x2)

| LCD | ESP8266 | GPIO |
| --- | ------- | ---- |
| SDA | D2      | 4    |
| SCL | D1      | 5    |
| VCC | 5V      | -    |
| GND | GND     | -    |

### Cảm biến DHT22

| DHT22 | ESP8266 | GPIO |
| ----- | ------- | ---- |
| DATA  | D5      | 14   |
| VCC   | 3.3V    | -    |
| GND   | GND     | -    |

### Cảm biến MQ2 (Gas)

| MQ2 | ESP8266 | GPIO |
| --- | ------- | ---- |
| AO  | A0      | ADC0 |
| VCC | 5V      | -    |
| GND | GND     | -    |

### Cảm biến PIR HC-SR501

| PIR | ESP8266 | GPIO |
| --- | ------- | ---- |
| OUT | D6      | 12   |
| VCC | 5V      | -    |
| GND | GND     | -    |

### LED

| LED     | ESP8266 | GPIO | Chức năng               |
| ------- | ------- | ---- | ----------------------- |
| LED PIR | D7      | 13   | Bật khi có người        |
| LED Gas | D0      | 16   | Nhấp nháy khi gas alert |

### Quạt DC (Module L9110H)

| L9110H | ESP8266 | GPIO |
| ------ | ------- | ---- |
| INA    | D3      | 0    |
| INB    | D4      | 2    |
| VCC    | 5V      | -    |
| GND    | GND     | -    |

### Nút bấm

| Button | ESP8266 | GPIO |
| ------ | ------- | ---- |
| FAN    | D8      | 15   |

---

## Logic hoạt động

### Trạng thái hệ thống

```
┌─────────────────┐
│  STATE_NORMAL   │ ←── Trạng thái bình thường
├─────────────────┤
│ - Đọc DHT22     │
│ - Đọc PIR       │
│ - Điều khiển đèn│
│ - Điều khiển quạt│
└────────┬────────┘
         │ Gas > 400
         ▼
┌─────────────────┐
│ STATE_GAS_ALERT │ ←── Cảnh báo gas
├─────────────────┤
│ - LED Gas nhấp  │
│ - LCD cảnh báo  │
│ - Gửi MQTT alert│
└─────────────────┘
```

### Khởi động (Setup)

```
1. Kết nối WiFi
2. Đồng bộ thời gian NTP (bắt buộc cho SSL)
3. Khởi tạo LCD, DHT, MQ2, PIR, LED, Fan, Button
4. Kết nối MQTT broker (TLS)
5. Subscribe topic: room1
```

### Vòng lặp chính (Loop)

```
1. Cập nhật Button (debounce)
2. Cập nhật LED Gas (blinking nếu alert)
3. Đọc MQ2 mỗi 1 giây
4. Nếu gas > ngưỡng → STATE_GAS_ALERT
5. Nếu STATE_NORMAL:
   - Đọc DHT22 mỗi 5 giây
   - Xử lý PIR (bật/tắt đèn)
   - Xử lý Button (toggle quạt)
6. Cập nhật LCD mỗi 1 giây
7. Xử lý MQTT (connect, receive, send)
```

### Logic PIR (Đèn tự động)

```
┌──────────────┐     Có chuyển động     ┌──────────────┐
│  LED OFF     │ ──────────────────────>│   LED ON     │
└──────────────┘                        └──────┬───────┘
       ▲                                       │
       │         Không chuyển động 10s         │
       └───────────────────────────────────────┘
```

### Hiển thị LCD

| Trạng thái  | Dòng 1            | Dòng 2           |
| ----------- | ----------------- | ---------------- |
| Bình thường | `T:25.5C H:65.0%` | `Fan:OFF Led:ON` |
| Gas Alert   | `!! GAS ALERT !!` | `Value: 517`     |

---

## 📡 MQTT Configuration

| Tham số   | Giá trị                                   |
| --------- | ----------------------------------------- |
| Broker    | `sdf8e281.ala.asia-southeast1.emqxsl.com` |
| Port      | `8883` (TLS)                              |
| Username  | `firmware_room`                           |
| Password  | `12345678`                                |
| Client ID | `esp8266_room_{ChipID}`                   |

### Topics

| Topic                 | Hướng | Mô tả                                    |
| --------------------- | ----- | ---------------------------------------- |
| `iot_smarthome/room1` | TX/RX | Gửi sensor data, nhận command từ backend |

---

## JSON Data Format

### 1. DHT Sensor Data (Room → Backend)

**Topic:** `iot_smarthome/room1`

```json
{
  "type": "sensor",
  "sensor": "dht",
  "temperature": 25.5,
  "humidity": 65.0,
  "timestamp": "2026-01-04 15:30:45"
}
```

| Trường      | Kiểu   | Mô tả                 |
| ----------- | ------ | --------------------- |
| type        | string | `"sensor"`            |
| sensor      | string | `"dht"`               |
| temperature | float  | Nhiệt độ (°C)         |
| humidity    | float  | Độ ẩm (%)             |
| timestamp   | string | Thời gian đọc (GMT+7) |

---

### 2. PIR Sensor Data (Room → Backend)

**Topic:** `iot_smarthome/room1`

#### Phát hiện chuyển động (LED ON)

```json
{
  "type": "sensor",
  "sensor": "pir",
  "motion": 1,
  "light": 1,
  "timestamp": "2026-01-04 15:30:45"
}
```

#### Không có chuyển động (LED OFF)

```json
{
  "type": "sensor",
  "sensor": "pir",
  "motion": 0,
  "light": 0,
  "timestamp": "2026-01-04 15:30:45"
}
```

| Trường    | Kiểu   | Mô tả                             |
| --------- | ------ | --------------------------------- |
| type      | string | `"sensor"`                        |
| sensor    | string | `"pir"`                           |
| motion    | int    | `1` = có chuyển động, `0` = không |
| light     | int    | `1` = đèn bật, `0` = đèn tắt      |
| timestamp | string | Thời gian sự kiện                 |

---

### 3. Gas Alert (Room → Backend/Door)

**Topic:** `iot_smarthome/room1`

#### Cảnh báo gas BẬT

```json
{
  "type": "gas",
  "event": "alert",
  "value": 517,
  "state": 1,
  "timestamp": "2026-01-04 15:30:45"
}
```

#### Gas alert đã clear

```json
{
  "type": "gas",
  "event": "clear",
  "state": 0,
  "timestamp": "2026-01-04 15:30:45"
}
```

| Trường    | Kiểu   | Mô tả                                |
| --------- | ------ | ------------------------------------ |
| type      | string | `"gas"`                              |
| event     | string | `"alert"` hoặc `"clear"`             |
| value     | int    | Giá trị cảm biến MQ2 (chỉ khi alert) |
| state     | int    | `1` = alert, `0` = clear             |
| timestamp | string | Thời gian sự kiện                    |

---

z

## Sequence Diagram

### Phát hiện Gas

```
┌──────┐       ┌──────┐       ┌─────────┐       ┌──────┐
│ MQ2  │       │ Room │       │ Backend │       │ Door │
└──┬───┘       └──┬───┘       └────┬────┘       └──┬───┘
   │  Gas > 400   │                │               │
   │─────────────>│                │               │
   │              │ LED Gas Blink  │               │
   │              │ LCD: GAS ALERT │               │
   │              │                │               │
   │              │  Gas Alert     │               │
   │              │───────────────>│               │
   │              │                │  Gas Alert    │
   │              │                │──────────────>│
   │              │                │               │
   │              │                │               │ LCD: GAS ALERT
   │              │                │               │
   │              │  Clear Command │               │
   │              │<───────────────│               │
   │              │                │               │
   │              │ LED Gas OFF    │               │
   │              │ LCD: Normal    │               │
```

### Phát hiện chuyển động

```
┌──────┐       ┌──────┐       ┌─────────┐
│ PIR  │       │ Room │       │ Backend │
└──┬───┘       └──┬───┘       └────┬────┘
   │  Motion      │                │
   │─────────────>│                │
   │              │ LED PIR ON     │
   │              │                │
   │              │  PIR Event     │
   │              │───────────────>│
   │              │                │
   │ (10s later)  │                │
   │ No motion    │                │
   │─────────────>│                │
   │              │ LED PIR OFF    │
   │              │                │
   │              │  PIR Event     │
   │              │───────────────>│
```

---

## Cấu hình

### WiFi (main.cpp)

```cpp
#define WIFI_SSID "IOT"
#define WIFI_PASS "20225211"
```

### NTP Timezone

```cpp
configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
// GMT+7 (Vietnam)
```

### Ngưỡng & Timing (pins.h)

```cpp
#define GAS_THRESHOLD       400     // Ngưỡng cảnh báo gas
#define PIR_TIMEOUT_MS      10000   // 10 giây tắt đèn
#define DHT_READ_INTERVAL   5000    // 5 giây đọc DHT
#define MQ2_READ_INTERVAL   1000    // 1 giây đọc MQ2
#define LCD_UPDATE_INTERVAL 1000    // 1 giây cập nhật LCD
#define BLINK_INTERVAL      250     // 250ms nhấp nháy LED
#define DEBOUNCE_DELAY      50      // 50ms debounce button
```

---

## Build & Upload

```bash
# Build
pio run


# Upload
pio run --target upload

# Monitor Serial
pio device monitor --baud 115200
```

---

## Serial Log Example

```
===== ROOM CONTROLLER (ESP8266) =====
[WiFi] Connecting to IOT
....[Time] Syncing.......
[Time] Time synced

[WiFi] Connected
[WiFi] IP: 192.168.176.50
[LCD] Init OK
[DHT] Init OK
[MQ2] Init OK
[PIR] Init OK
[System] Setup complete
[MQTT] Connecting... OK
[MQTT] Subscribed: iot_smarthome/room1room1
[MQTT] TX room1 -> {"type":"sensor","sensor":"dht","temperature":25.5,"humidity":65.0,"timestamp":"2026-01-04 15:30:45"}
[PIR] Motion detected! LED ON
[MQTT] TX room1 -> {"type":"sensor","sensor":"pir","motion":1,"light":1,"timestamp":"2026-01-04 15:30:50"}
[BUTTON] Fan toggled: ON
[MQTT] TX room1 -> {"type":"device","device":"fan","state":1,"timestamp":"2026-01-04 15:31:00"}
[MQ2] Gas alert! Value: 517
[MQTT] TX room1 -> {"type":"gas","event":"alert","value":517,"state":1,"timestamp":"2026-01-04 15:32:00"}
[MQTT] RX room1 -> {"type":"command","device":"gas","action":"clear"}
[MQTT] TX room1 -> {"type":"gas","event":"clear","state":0,"timestamp":"2026-01-04 15:33:00"}
```

---

## Tổng hợp JSON Format

### Từ Room gửi đi (TX)

| Sự kiện    | type     | Các trường                               |
| ---------- | -------- | ---------------------------------------- |
| DHT data   | `sensor` | sensor, temperature, humidity, timestamp |
| PIR motion | `sensor` | sensor, motion, light, timestamp         |
| Gas alert  | `gas`    | event, value, state, timestamp           |
| Gas clear  | `gas`    | event, state, timestamp                  |
| Fan toggle | `device` | device, state, timestamp                 |
| LED toggle | `device` | device, state, timestamp                 |

### Từ Backend nhận (RX)

| Command     | type      | Các trường                         |
| ----------- | --------- | ---------------------------------- |
| Fan control | `command` | device: `"fan"`, state             |
| LED control | `command` | device: `"led_pir"`, state         |
| Gas clear   | `command` | device: `"gas"`, action: `"clear"` |

---

## Author

IoT Smart Home Project - 2026
