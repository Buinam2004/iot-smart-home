# IoT Management System API

Web API quản lý hệ thống IoT được xây dựng bằng Java Spring Boot, EntityFramework, và MySQL.

## Công nghệ sử dụng

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL Database**
- **Maven**
- **Lombok**

## Cấu trúc dự án

```
src/main/java/com/iot/
├── entity/              # Các entity classes
│   ├── User.java
│   ├── Device.java
│   └── SensorData.java
├── dto/                 # Data Transfer Objects
│   ├── DeviceDTO.java
│   └── SensorDataDTO.java
├── repository/          # JPA Repositories
│   ├── UserRepository.java
│   ├── DeviceRepository.java
│   └── SensorDataRepository.java
├── service/             # Business logic
│   ├── UserService.java
│   ├── DeviceService.java
│   └── SensorDataService.java
├── controller/          # REST Controllers
│   ├── UserController.java
│   ├── DeviceController.java
│   └── SensorDataController.java
└── IotManagementApplication.java
```

## Cấu hình Database

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/iot_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

## Cài đặt và chạy

### 1. Yêu cầu hệ thống

- JDK 17 trở lên
- Maven 3.6+
- MySQL 8.0+

### 2. Cài đặt MySQL

Tạo database (hoặc để Spring Boot tự tạo):

```sql
CREATE DATABASE iot_db;
```

### 3. Build và chạy project

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Hoặc sử dụng IDE (IntelliJ IDEA, Eclipse):
- Import project as Maven project
- Run `IotManagementApplication.java`

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## API Endpoints

### User API

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/users` | Lấy tất cả users |
| GET | `/api/users/{id}` | Lấy user theo ID |
| POST | `/api/users` | Tạo user mới |
| PUT | `/api/users/{id}` | Cập nhật user |
| DELETE | `/api/users/{id}` | Xóa user |

**Ví dụ tạo User:**
```json
POST /api/users
{
  "username": "admin",
  "password": "123456",
  "email": "admin@example.com"
}
```

### Device API

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/devices` | Lấy tất cả devices (kèm tên người tạo) |
| GET | `/api/devices?creatorName=username` | Tìm kiếm theo tên người tạo |
| GET | `/api/devices?offline=true` | Lấy danh sách thiết bị offline |
| GET | `/api/devices/{id}` | Lấy device theo ID |
| POST | `/api/devices` | Tạo device mới |
| PUT | `/api/devices/{id}` | Cập nhật device |
| DELETE | `/api/devices/{id}` | Xóa device |

**Ví dụ tạo Device:**
```json
POST /api/devices
{
  "user_id": 1,
  "name": "Temperature Sensor 1",
  "device_key": "TEMP001",
  "is_online": true
}
```

**Ví dụ query parameters:**
- `GET /api/devices?creatorName=admin` - Tìm thiết bị của user "admin"
- `GET /api/devices?offline=true` - Lấy thiết bị offline

### SensorData API

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/sensor-data` | Lấy tất cả sensor data (kèm tên thiết bị) |
| GET | `/api/sensor-data?deviceName=name` | Tìm kiếm theo tên thiết bị |
| GET | `/api/sensor-data?recentSeconds=60` | Lấy dữ liệu trong 60 giây gần nhất |
| GET | `/api/sensor-data/{id}` | Lấy sensor data theo ID |
| POST | `/api/sensor-data` | Tạo sensor data mới |
| PUT | `/api/sensor-data/{id}` | Cập nhật sensor data |
| DELETE | `/api/sensor-data/{id}` | Xóa sensor data |

**Ví dụ tạo SensorData:**
```json
POST /api/sensor-data
{
  "device_id": 1,
  "type": "temperature",
  "value": 25.5
}
```

**Ví dụ query parameters:**
- `GET /api/sensor-data?deviceName=Temperature Sensor 1` - Lấy dữ liệu của thiết bị
- `GET /api/sensor-data?recentSeconds=60` - Lấy dữ liệu trong 60 giây gần nhất

## Các tính năng đặc biệt

### Device
- ✅ Lấy danh sách thiết bị kèm theo tên người tạo
- ✅ Tìm kiếm thiết bị theo tên người tạo (query parameter)
- ✅ Lấy danh sách thiết bị offline (query parameter)

### SensorData
- ✅ Lấy danh sách dữ liệu kèm theo tên thiết bị
- ✅ Tìm kiếm dữ liệu theo tên thiết bị (query parameter)
- ✅ Lấy dữ liệu trong khoảng thời gian gần nhất tính theo giây (query parameter)

## Testing API

Có thể sử dụng các công cụ sau để test API:
- **Postman**
- **curl**
- **Swagger UI** (có thể thêm spring-doc dependency)

### Ví dụ với curl:

```bash
# Tạo user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","email":"admin@example.com"}'

# Lấy danh sách users
curl http://localhost:8080/api/users

# Tạo device
curl -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"name":"Sensor 1","deviceKey":"KEY001","isOnline":true}'

# Lấy thiết bị offline
curl "http://localhost:8080/api/devices?offline=true"

# Tạo sensor data
curl -X POST http://localhost:8080/api/sensor-data \
  -H "Content-Type: application/json" \
  -d '{"deviceId":1,"type":"temperature","value":25.5}'

# Lấy dữ liệu trong 60 giây gần nhất
curl "http://localhost:8080/api/sensor-data?recentSeconds=60"
```

## Lưu ý

1. Đảm bảo MySQL đang chạy trước khi start ứng dụng
2. Cập nhật thông tin database trong `application.properties` nếu cần
3. Database sẽ được tự động tạo các bảng khi ứng dụng chạy lần đầu (ddl-auto=update)
4. Tất cả các API đều hỗ trợ CORS (`@CrossOrigin(origins = "*")`)

## License

MIT License
