# DOOR CONTROLLER - ESP8266 Firmware

## Tổng quan

Firmware điều khiển cửa thông minh sử dụng ESP8266 (NodeMCU), tích hợp:

- **RFID RC522** - Đọc thẻ từ
- **Servo SG90** - Điều khiển cửa
- **LCD I2C 16x2** - Hiển thị trạng thái
- **MQTT over TLS** - Giao tiếp với backend và các thiết bị khác

---

## Cấu trúc thư mục

```
firmware_door/
├── include/
│   ├── pins.h              # Định nghĩa chân GPIO
│   ├── lcd_display.h       # API màn hình LCD
│   ├── rfid_reader.h       # API đọc thẻ RFID
│   ├── servo_door.h        # API điều khiển servo
│   ├── mqtt_client.h       # API MQTT client
│   └── smarthome_door.h    # Logic điều khiển chính
├── src/
│   ├── main.cpp            # Entry point, WiFi, NTP
│   ├── lcd_display.cpp     # Hiển thị LCD
│   ├── rfid_reader.cpp     # Đọc RFID RC522
│   ├── servo_door.cpp      # Điều khiển servo
│   ├── mqtt_client.cpp     # MQTT client với SSL
│   └── smarthome_door.cpp  # Logic xử lý chính
├── platformio.ini          # Cấu hình PlatformIO
└── README.md               # File này
```

---

## Sơ đồ chân kết nối (ESP8266 NodeMCU)

### RFID RC522

| RC522 | ESP8266 | GPIO |
| ----- | ------- | ---- |
| SDA   | D8      | 15   |
| SCK   | D5      | 14   |
| MOSI  | D7      | 13   |
| MISO  | D6      | 12   |
| RST   | D0      | 16   |
| 3.3V  | 3.3V    | -    |
| GND   | GND     | -    |

### LCD I2C (16x2)

| LCD | ESP8266 | GPIO |
| --- | ------- | ---- |
| SDA | D2      | 4    |
| SCL | D1      | 5    |
| VCC | 5V      | -    |
| GND | GND     | -    |

### Servo SG90

| Servo  | ESP8266 | GPIO |
| ------ | ------- | ---- |
| Signal | D4      | 2    |
| VCC    | 5V      | -    |
| GND    | GND     | -    |

---

## Logic hoạt động

### Khởi động (Setup)

```
1. Kết nối WiFi
2. Đồng bộ thời gian NTP (bắt buộc cho SSL)
3. Khởi tạo RFID, Servo, LCD
4. Kết nối MQTT broker (TLS)
5. Subscribe topics: door1, room1
```

### Vòng lặp chính (Loop)

```
1. Kiểm tra kết nối MQTT, reconnect nếu cần
2. Xử lý message MQTT nhận được
3. Đọc thẻ RFID, gửi event lên MQTT
4. Tự động đóng cửa sau 7 giây
```

### Hiển thị LCD

| Trạng thái             | Dòng 1              | Dòng 2              |
| ---------------------- | ------------------- | ------------------- |
| Bình thường (DHT data) | `Temp: 21.2°C`      | `Hum: 74.8%`        |
| Quẹt thẻ               | `CARD DETECTED`     | `SCANNING...`       |
| Đang kiểm tra          | `CHECKING...`       | `PLEASE WAIT`       |
| Mở cửa                 | `WELCOME`           | `DOOR OPEN`         |
| Từ chối                | `ACCESS DENIED`     |                     |
| Cảnh báo gas           | `!!! GAS ALERT !!!` | `CHECK IMMEDIATELY` |

---

## MQTT Configuration

| Tham số   | Giá trị                                   |
| --------- | ----------------------------------------- |
| Broker    | `sdf8e281.ala.asia-southeast1.emqxsl.com` |
| Port      | `8883` (TLS)                              |
| Username  | `firmware_door`                           |
| Password  | `12345678`                                |
| Client ID | `esp8266_door_{ChipID}`                   |

### Topics

| Topic                 | Hướng | Mô tả                                   |
| --------------------- | ----- | --------------------------------------- |
| `iot_smarthome/door1` | TX/RX | Gửi event RFID, nhận command từ backend |
| `iot_smarthome/room1` | RX    | Nhận data từ Room1 (DHT, Gas)           |

---

## JSON Data Format

### 1. RFID Scan Event (Door → Backend)

**Topic:** `iot_smarthome/door1`

```json
{
  "type": "rfid",
  "event": "scan",
  "uid": "31AB1916",
  "timestamp": "2026-01-04 14:55:07"
}
```

| Trường    | Kiểu   | Mô tả                        |
| --------- | ------ | ---------------------------- |
| type      | string | Loại dữ liệu: `"rfid"`       |
| event     | string | Sự kiện: `"scan"`            |
| uid       | string | Mã thẻ RFID (HEX, uppercase) |
| timestamp | string | Thời gian quẹt (GMT+7)       |

---

### 2. Door Command (Backend → Door)

**Topic:** `iot_smarthome/door1`

#### Mở cửa

```json
{
  "type": "command",
  "device": "door",
  "action": "open"
}
```

#### Từ chối

```json
{
  "type": "command",
  "device": "door",
  "action": "deny"
}
```

| Trường | Kiểu   | Giá trị                |
| ------ | ------ | ---------------------- |
| type   | string | `"command"`            |
| device | string | `"door"`               |
| action | string | `"open"` hoặc `"deny"` |

---

### 3. DHT Sensor Data (Room1 → Door)

**Topic:** `iot_smarthome/room1`

```json
{
  "type": "sensor",
  "sensor": "dht",
  "temperature": 21.2,
  "humidity": 74.8
}
```

| Trường      | Kiểu   | Mô tả         |
| ----------- | ------ | ------------- |
| type        | string | `"sensor"`    |
| sensor      | string | `"dht"`       |
| temperature | float  | Nhiệt độ (°C) |
| humidity    | float  | Độ ẩm (%)     |

**Hành vi:** LCD hiển thị nhiệt độ và độ ẩm

---

### 4. Gas Alert (Room1 → Door)

**Topic:** `iot_smarthome/room1`

#### Cảnh báo gas BẬT

```json
{
  "type": "gas",
  "event": "alert",
  "value": 517,
  "state": 1
}
```

| Trường | Kiểu   | Mô tả                |
| ------ | ------ | -------------------- |
| type   | string | `"gas"`              |
| event  | string | `"alert"`            |
| value  | int    | Giá trị cảm biến MQ2 |
| state  | int    | `1` = BẬT cảnh báo   |

**Hành vi:** LCD hiển thị "!!! GAS ALERT !!!"

---

### 5. Gas Clear Command (Backend → Door)

**Topic:** `iot_smarthome/door1`

```json
{
  "type": "command",
  "device": "gas",
  "action": "clear"
}
```

| Trường | Kiểu   | Giá trị     |
| ------ | ------ | ----------- |
| type   | string | `"command"` |
| device | string | `"gas"`     |
| action | string | `"clear"`   |

**Hành vi:** LCD trở về hiển thị nhiệt độ/độ ẩm

---

## Sequence Diagram

### Quẹt thẻ thành công

```
┌──────┐       ┌──────┐       ┌─────────┐
│ RFID │       │ Door │       │ Backend │
└──┬───┘       └──┬───┘       └────┬────┘
   │   Quẹt thẻ   │                │
   │─────────────>│                │
   │              │ LCD: SCANNING  │
   │              │ LCD: CHECKING  │
   │              │                │
   │              │  RFID Event    │
   │              │───────────────>│
   │              │                │
   │              │  Command:OPEN  │
   │              │<───────────────│
   │              │                │
   │              │ LCD: WELCOME   │
   │              │ Servo: OPEN    │
   │              │                │
   │              │ (7s later)     │
   │              │ Servo: CLOSE   │
   │              │ LCD: Temp/Hum  │
```

### Cảnh báo Gas

```
┌───────┐       ┌──────┐       ┌─────────┐
│ Room1 │       │ Door │       │ Backend │
└───┬───┘       └──┬───┘       └────┬────┘
    │  Gas Alert   │                │
    │─────────────>│                │
    │              │                │
    │              │ LCD: GAS ALERT │
    │              │                │
    │              │  Clear Command │
    │              │<───────────────│
    │              │                │
    │              │ LCD: Temp/Hum  │
```

## 👥 Author

IoT Smart Home Project - 2026
