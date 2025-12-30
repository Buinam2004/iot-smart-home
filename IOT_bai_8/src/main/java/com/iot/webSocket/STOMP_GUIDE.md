# Hướng Dẫn Chi Tiết STOMP từ A-Z

## Mục Lục
1. [STOMP Là Gì?](#stomp-là-gì)
2. [Kiến Trúc và Hoạt Động](#kiến-trúc-và-hoạt-động)
3. [Cấu Hình Spring Boot + STOMP](#cấu-hình-spring-boot--stomp)
4. [Các Loại Frame STOMP](#các-loại-frame-stomp)
5. [Implementing STOMP trong IoT Project](#implementing-stomp-trong-iot-project)
6. [Message Broker và Destination](#message-broker-và-destination)
7. [Authentication và Security](#authentication-và-security)
8. [Testing STOMP Connection](#testing-stomp-connection)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## STOMP Là Gì?

### Định Nghĩa
**STOMP** (Simple Text Oriented Messaging Protocol) là một giao thức messaging đơn giản dựa trên text, được thiết kế để làm việc với message broker. STOMP cung cấp một định dạng khung (frame) có thể tương tác được, cho phép các STOMP client giao tiếp với bất kỳ STOMP message broker nào.

### Tại Sao Sử Dụng STOMP?

#### Ưu Điểm:
- **Đơn giản**: Dễ implement và debug vì là text-based protocol
- **Language-agnostic**: Có client cho nhiều ngôn ngữ (Java, JavaScript, Python, etc.)
- **Standardized**: Giao thức chuẩn với specification rõ ràng
- **Reliable messaging**: Hỗ trợ acknowledgment và transaction
- **Scalable**: Hoạt động tốt với message broker như RabbitMQ, ActiveMQ

#### So Sánh với WebSocket thuần:
```
WebSocket thuần:
- Chỉ cung cấp connection layer
- Không có message routing
- Không có message format chuẩn
- Phải tự implement logic messaging

STOMP over WebSocket:
- Connection layer + Messaging protocol
- Built-in routing với destination
- Standardized frame format
- Pub/Sub pattern có sẵn
```

---

## Kiến Trúc và Hoạt Động

### Kiến Trúc Tổng Quan

```
┌─────────────┐         WebSocket         ┌─────────────────┐
│   Client    │◄──────────────────────────►│  STOMP Broker   │
│ (Browser/   │      STOMP Protocol        │  (Spring Boot)  │
│   Mobile)   │                            │                 │
└─────────────┘                            └─────────────────┘
       │                                            │
       │ 1. CONNECT                                 │
       ├───────────────────────────────────────────►│
       │                                            │
       │ 2. CONNECTED                               │
       │◄───────────────────────────────────────────┤
       │                                            │
       │ 3. SUBSCRIBE to /topic/sensor-data         │
       ├───────────────────────────────────────────►│
       │                                            │
       │ 4. SEND to /app/update-sensor              │
       ├───────────────────────────────────────────►│
       │                                            │ Process
       │ 5. MESSAGE from /topic/sensor-data         │
       │◄───────────────────────────────────────────┤
       │                                            │
```

### Flow Chi Tiết

#### Bước 1: Thiết Lập Connection
```javascript
// Client JavaScript
const socket = new SockJS('/ws-endpoint');
const stompClient = Stomp.over(socket);

stompClient.connect(
  headers,              // Authentication headers
  onConnectCallback,    // Success callback
  onErrorCallback       // Error callback
);
```

**Điều gì xảy ra:**
1. SockJS tạo WebSocket connection đến server
2. STOMP client gửi CONNECT frame với headers
3. Server xác thực và phản hồi CONNECTED frame
4. Connection được thiết lập

#### Bước 2: Subscribe to Topics
```javascript
stompClient.subscribe('/topic/sensor-data', (message) => {
  const data = JSON.parse(message.body);
  console.log('Received:', data);
});
```

**Điều gì xảy ra:**
1. Client gửi SUBSCRIBE frame với destination
2. Server đăng ký client vào topic
3. Mọi message gửi đến topic sẽ được forward tới client này

#### Bước 3: Gửi Message
```javascript
stompClient.send(
  '/app/update-sensor',        // Destination
  {},                          // Headers
  JSON.stringify(sensorData)   // Body
);
```

**Điều gì xảy ra:**
1. Client gửi SEND frame đến destination
2. Spring's @MessageMapping xử lý message
3. Server có thể broadcast kết quả đến các subscriber

---

## Cấu Hình Spring Boot + STOMP

### 1. Dependencies (pom.xml)

```xml
<dependencies>
    <!-- WebSocket Support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- STOMP Messaging -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-messaging</artifactId>
    </dependency>
    
    <!-- SockJS (Optional, for browser compatibility) -->
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>sockjs-client</artifactId>
        <version>1.5.1</version>
    </dependency>
    
    <!-- STOMP WebSocket (Optional, for client) -->
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>stomp-websocket</artifactId>
        <version>2.3.4</version>
    </dependency>
</dependencies>
```

### 2. WebSocket Configuration Class

```java
package com.iot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker  // Kích hoạt STOMP over WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Đăng ký STOMP endpoints - nơi client connect
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")           // Endpoint path
                .setAllowedOriginPatterns("*") // CORS configuration
                .withSockJS();                 // Enable SockJS fallback
        
        // Có thể thêm endpoint khác không dùng SockJS
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Cấu hình message broker - xử lý routing của messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker
        config.enableSimpleBroker(
            "/topic",    // Prefix cho broadcast messages (1-to-many)
            "/queue"     // Prefix cho point-to-point messages (1-to-1)
        );
        
        // Prefix cho các message gửi từ client
        config.setApplicationDestinationPrefixes("/app");
        
        // Optional: Prefix cho user-specific messages
        config.setUserDestinationPrefix("/user");
    }
}
```

### 3. Giải Thích Chi Tiết Các Prefix

#### Application Destination Prefix (`/app`)
```
Client gửi: /app/update-sensor
Server nhận tại: @MessageMapping("/update-sensor")
```
- Dùng cho messages từ client → server
- Spring routing đến @MessageMapping methods

#### Topic Prefix (`/topic`)
```
Server broadcast: /topic/sensor-data
Client subscribe: /topic/sensor-data
```
- Dùng cho pub/sub pattern (1-to-many)
- Tất cả subscribers nhận message

#### Queue Prefix (`/queue`)
```
Server gửi: /queue/notifications-{userId}
Client subscribe: /queue/notifications-{userId}
```
- Dùng cho point-to-point messaging (1-to-1)
- Chỉ một client cụ thể nhận message

#### User Destination Prefix (`/user`)
```
Server gửi đến user: /user/{username}/queue/notifications
Client subscribe: /user/queue/notifications
```
- Framework tự động thêm username
- Dùng cho user-specific messages

---

## Các Loại Frame STOMP

### 1. CONNECT Frame
```
CONNECT
accept-version:1.2
host:stomp.example.com
login:admin
passcode:secret123

^@
```

**Headers quan trọng:**
- `accept-version`: Phiên bản STOMP hỗ trợ
- `host`: Virtual host (cho multi-tenancy)
- `login/passcode`: Authentication credentials
- `heart-beat`: Keep-alive configuration

### 2. CONNECTED Frame
```
CONNECTED
version:1.2
heart-beat:0,0
session:session-123

^@
```

**Server response khi connection thành công:**
- `version`: Phiên bản STOMP được chọn
- `session`: Session identifier
- `heart-beat`: Server's heartbeat configuration

### 3. SEND Frame
```
SEND
destination:/app/update-sensor
content-type:application/json
content-length:45

{"deviceId":"DEV001","temperature":25.5}^@
```

**Gửi message từ client:**
- `destination`: Đích của message (routing key)
- `content-type`: MIME type của body
- `content-length`: Độ dài body (optional nhưng recommended)

### 4. SUBSCRIBE Frame
```
SUBSCRIBE
id:sub-0
destination:/topic/sensor-data
ack:auto

^@
```

**Đăng ký nhận messages:**
- `id`: Subscription identifier (unique per connection)
- `destination`: Topic/queue để subscribe
- `ack`: Acknowledgment mode (auto/client/client-individual)

### 5. MESSAGE Frame
```
MESSAGE
subscription:sub-0
message-id:msg-123
destination:/topic/sensor-data
content-type:application/json

{"deviceId":"DEV001","temperature":25.5}^@
```

**Server gửi đến subscriber:**
- `subscription`: ID từ SUBSCRIBE frame
- `message-id`: Unique message identifier
- `destination`: Destination gốc

### 6. UNSUBSCRIBE Frame
```
UNSUBSCRIBE
id:sub-0

^@
```

**Hủy subscription:**
- `id`: Subscription ID cần hủy

### 7. DISCONNECT Frame
```
DISCONNECT
receipt:receipt-123

^@
```

**Graceful disconnect:**
- `receipt`: Request receipt confirmation

### 8. ERROR Frame
```
ERROR
message:Access denied

User not authorized to access /topic/admin^@
```

**Server gửi khi có lỗi:**
- `message`: Error description
- Body: Detailed error information

---

## Implementing STOMP trong IoT Project

### 1. Controller với Message Mapping

```java
package com.iot.controller;

import com.iot.dto.SensorDataDTO;
import com.iot.service.ISensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    @Autowired
    private ISensorDataService sensorDataService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Nhận sensor data từ device và broadcast đến tất cả clients
     * 
     * Client gửi đến: /app/sensor/update
     * Server broadcast đến: /topic/sensor-data
     */
    @MessageMapping("/sensor/update")
    @SendTo("/topic/sensor-data")
    public SensorDataDTO handleSensorUpdate(SensorDataDTO sensorData) {
        // Lưu vào database
        SensorDataDTO saved = sensorDataService.createSensorData(sensorData);
        
        // Return value sẽ được broadcast đến /topic/sensor-data
        return saved;
    }

    /**
     * Gửi notification cho specific user
     * 
     * Client gửi đến: /app/device/alert
     * Server gửi đến: /user/{username}/queue/alerts
     */
    @MessageMapping("/device/alert")
    @SendToUser("/queue/alerts")
    public String handleDeviceAlert(
        @Payload String alertMessage,
        Principal principal  // Tự động inject authenticated user
    ) {
        String username = principal.getName();
        System.out.println("Alert from user: " + username);
        return "Alert processed: " + alertMessage;
    }

    /**
     * Xử lý message với custom headers
     */
    @MessageMapping("/sensor/batch")
    public void handleBatchUpdate(
        @Payload SensorDataDTO[] sensorDataArray,
        @Header("device-id") String deviceId,
        @Header("priority") String priority
    ) {
        System.out.println("Batch update from device: " + deviceId);
        System.out.println("Priority: " + priority);
        
        // Process batch
        for (SensorDataDTO data : sensorDataArray) {
            sensorDataService.createSensorData(data);
        }
        
        // Send confirmation to specific topic
        messagingTemplate.convertAndSend(
            "/topic/device/" + deviceId + "/status",
            "Batch processed successfully"
        );
    }

    /**
     * Gửi message đến specific user programmatically
     */
    public void sendToUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            message
        );
    }

    /**
     * Broadcast message đến tất cả subscribers
     */
    public void broadcastSensorData(SensorDataDTO data) {
        messagingTemplate.convertAndSend(
            "/topic/sensor-data",
            data
        );
    }

    /**
     * Gửi message với custom headers
     */
    public void sendWithHeaders(String destination, Object payload, String deviceId) {
        messagingTemplate.convertAndSend(
            destination,
            payload,
            Map.of("device-id", deviceId, "timestamp", System.currentTimeMillis())
        );
    }
}
```

### 2. Service Layer Broadcasting

```java
package com.iot.service;

import com.iot.dto.SensorDataDTO;
import com.iot.entity.SensorData;
import com.iot.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorDataService implements ISensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public SensorDataDTO createSensorData(SensorDataDTO dto) {
        // Lưu vào database
        SensorData entity = convertToEntity(dto);
        SensorData saved = sensorDataRepository.save(entity);
        
        // Convert back to DTO
        SensorDataDTO result = convertToDTO(saved);
        
        // Real-time broadcast đến tất cả connected clients
        broadcastSensorUpdate(result);
        
        // Nếu giá trị vượt threshold, gửi alert
        if (result.getTemperature() > 30.0) {
            sendAlertToDeviceOwner(result);
        }
        
        return result;
    }

    private void broadcastSensorUpdate(SensorDataDTO data) {
        // Broadcast đến topic chung
        messagingTemplate.convertAndSend(
            "/topic/sensor-data",
            data
        );
        
        // Broadcast đến topic specific của device
        messagingTemplate.convertAndSend(
            "/topic/device/" + data.getDeviceId() + "/sensor",
            data
        );
    }

    private void sendAlertToDeviceOwner(SensorDataDTO data) {
        String username = getDeviceOwnerUsername(data.getDeviceId());
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/alerts",
            "Temperature alert: " + data.getTemperature() + "°C"
        );
    }
    
    // Helper methods...
}
```

### 3. Event Listeners

```java
package com.iot.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Khi có client connect
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println("New WebSocket connection: " + sessionId);
        
        // Có thể gửi welcome message
        messagingTemplate.convertAndSend(
            "/topic/system",
            "New client connected: " + sessionId
        );
    }

    /**
     * Khi client disconnect
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? 
                         headerAccessor.getUser().getName() : "Unknown";
        
        System.out.println("Client disconnected: " + sessionId + " (User: " + username + ")");
        
        // Cleanup logic nếu cần
        cleanupUserSession(sessionId, username);
    }

    /**
     * Khi có client subscribe
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println("Client " + sessionId + " subscribed to: " + destination);
    }

    /**
     * Khi client unsubscribe
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = headerAccessor.getSubscriptionId();
        
        System.out.println("Client unsubscribed: " + subscriptionId);
    }

    private void cleanupUserSession(String sessionId, String username) {
        // Implement cleanup logic
        // Ví dụ: remove từ active users list, save last activity, etc.
    }
}
```

---

## Message Broker và Destination

### 1. Simple In-Memory Broker

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    // Simple broker - chạy trong memory của application
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
}
```

**Đặc điểm:**
- ✅ Dễ setup, không cần external dependency
- ✅ Tốt cho development và small-scale apps
- ❌ Không persist messages
- ❌ Không scale across multiple instances
- ❌ Mất messages khi restart

### 2. External Message Broker (RabbitMQ)

```xml
<!-- Thêm vào pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-reactor-netty</artifactId>
</dependency>
```

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable RabbitMQ broker
        config.enableStompBrokerRelay("/topic", "/queue")
              .setRelayHost("localhost")
              .setRelayPort(61613)          // STOMP port
              .setClientLogin("guest")
              .setClientPasscode("guest")
              .setSystemLogin("guest")
              .setSystemPasscode("guest");
        
        config.setApplicationDestinationPrefixes("/app");
    }
    
    // ... other configurations
}
```

**Đặc điểm:**
- ✅ Message persistence
- ✅ Scale across multiple instances
- ✅ Advanced routing và features
- ✅ High availability
- ❌ Cần setup external broker
- ❌ Phức tạp hơn

### 3. Destination Patterns

#### Wildcard Subscriptions
```java
// Subscribe với wildcard
stompClient.subscribe('/topic/device/*', callback);

// Sẽ nhận messages từ:
// /topic/device/DEV001
// /topic/device/DEV002
// etc.
```

#### Hierarchical Topics
```java
// Tổ chức theo hierarchy
/topic/sensor/temperature
/topic/sensor/humidity
/topic/sensor/pressure

/topic/device/DEV001/status
/topic/device/DEV001/sensor
/topic/device/DEV001/alerts
```

#### Dynamic Destinations
```java
@MessageMapping("/device/{deviceId}/command")
public void handleDeviceCommand(
    @DestinationVariable String deviceId,
    @Payload String command
) {
    System.out.println("Command for device " + deviceId + ": " + command);
    
    // Send response to device-specific topic
    messagingTemplate.convertAndSend(
        "/topic/device/" + deviceId + "/response",
        "Command executed: " + command
    );
}
```

---

## Authentication và Security

### 1. WebSocket Security Configuration

```java
package com.iot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig 
    extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Cho phép CONNECT và SUBSCRIBE không cần auth
            .nullDestMatcher().permitAll()
            
            // Các message đến /app/public/** không cần auth
            .simpDestMatchers("/app/public/**").permitAll()
            
            // Message đến /app/admin/** chỉ ADMIN
            .simpDestMatchers("/app/admin/**").hasRole("ADMIN")
            
            // Subscribe /topic/public/** không cần auth
            .simpSubscribeDestMatchers("/topic/public/**").permitAll()
            
            // Subscribe /user/** cần authenticated
            .simpSubscribeDestMatchers("/user/**").authenticated()
            
            // Tất cả messages khác cần authenticated
            .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF cho WebSocket (nếu cần)
        return true;
    }
}
```

### 2. JWT Authentication cho WebSocket

```java
package com.iot.config;

import com.iot.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ xử lý CONNECT command
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy JWT token từ header
            String authToken = accessor.getFirstNativeHeader("Authorization");
            
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);
                
                // Validate token
                if (jwtTokenProvider.validateToken(token)) {
                    // Extract user info
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    Authentication authentication = 
                        jwtTokenProvider.getAuthentication(token);
                    
                    // Set user trong STOMP session
                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                } else {
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            } else {
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }
        
        return message;
    }
}
```

```java
// Đăng ký interceptor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthenticationInterceptor authInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
    
    // ... other configurations
}
```

### 3. Client-side Authentication

```javascript
// JavaScript client với JWT
const token = localStorage.getItem('jwt_token');

const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Pass token trong CONNECT frame
const headers = {
  'Authorization': `Bearer ${token}`
};

stompClient.connect(headers, 
  (frame) => {
    console.log('Connected:', frame);
    
    // Subscribe sau khi authenticated
    stompClient.subscribe('/user/queue/notifications', (message) => {
      console.log('Notification:', message.body);
    });
  },
  (error) => {
    console.error('Connection error:', error);
  }
);
```

---

## Testing STOMP Connection

### 1. Testing với JavaScript Client

```html
<!DOCTYPE html>
<html>
<head>
    <title>STOMP Test Client</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>STOMP WebSocket Test</h1>
    
    <div>
        <button onclick="connect()">Connect</button>
        <button onclick="disconnect()">Disconnect</button>
    </div>
    
    <div>
        <input id="deviceId" placeholder="Device ID" value="DEV001" />
        <input id="temperature" placeholder="Temperature" value="25.5" />
        <button onclick="sendSensorData()">Send Sensor Data</button>
    </div>
    
    <div>
        <h3>Received Messages:</h3>
        <div id="messages"></div>
    </div>

    <script>
        let stompClient = null;

        function connect() {
            const socket = new SockJS('http://localhost:8080/ws');
            stompClient = Stomp.over(socket);
            
            // Debug mode
            stompClient.debug = (str) => {
                console.log('STOMP:', str);
            };

            const headers = {
                // Add JWT token if needed
                // 'Authorization': 'Bearer ' + token
            };

            stompClient.connect(headers,
                (frame) => {
                    console.log('Connected:', frame);
                    addMessage('Connected to server');
                    
                    // Subscribe to sensor data updates
                    stompClient.subscribe('/topic/sensor-data', (message) => {
                        const data = JSON.parse(message.body);
                        addMessage('Sensor Update: ' + JSON.stringify(data, null, 2));
                    });
                    
                    // Subscribe to user-specific notifications
                    stompClient.subscribe('/user/queue/notifications', (message) => {
                        addMessage('Notification: ' + message.body);
                    });
                },
                (error) => {
                    console.error('Error:', error);
                    addMessage('Error: ' + error);
                }
            );
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect(() => {
                    console.log('Disconnected');
                    addMessage('Disconnected from server');
                });
            }
        }

        function sendSensorData() {
            const deviceId = document.getElementById('deviceId').value;
            const temperature = parseFloat(document.getElementById('temperature').value);
            
            const sensorData = {
                deviceId: deviceId,
                temperature: temperature,
                humidity: 60.0,
                timestamp: new Date().toISOString()
            };

            stompClient.send(
                '/app/sensor/update',
                {},
                JSON.stringify(sensorData)
            );
            
            addMessage('Sent: ' + JSON.stringify(sensorData));
        }

        function addMessage(message) {
            const messagesDiv = document.getElementById('messages');
            const messageElement = document.createElement('div');
            messageElement.textContent = new Date().toLocaleTimeString() + ' - ' + message;
            messagesDiv.appendChild(messageElement);
        }
    </script>
</body>
</html>
```

### 2. Testing với Postman

**Bước 1: Tạo WebSocket Request**
```
URL: ws://localhost:8080/ws
Type: WebSocket (nếu support) hoặc dùng SockJS
```

**Bước 2: Gửi CONNECT Frame**
```
CONNECT
accept-version:1.2
heart-beat:10000,10000

^@
```

**Bước 3: Subscribe**
```
SUBSCRIBE
id:sub-1
destination:/topic/sensor-data

^@
```

**Bước 4: Send Message**
```
SEND
destination:/app/sensor/update
content-type:application/json

{"deviceId":"DEV001","temperature":25.5}^@
```

### 3. Testing với Spring Boot Test

```java
package com.iot.websocket;

import com.iot.dto.SensorDataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    public void setup() {
        wsUrl = "ws://localhost:" + port + "/ws";
        
        SockJsClient sockJsClient = new SockJsClient(
            List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );
        
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    public void testWebSocketConnection() throws Exception {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                completableFuture.complete("Connected");
            }
        };

        stompClient.connect(wsUrl, sessionHandler);
        
        String result = completableFuture.get(5, TimeUnit.SECONDS);
        assertEquals("Connected", result);
    }

    @Test
    public void testSendAndReceiveSensorData() throws Exception {
        BlockingQueue<SensorDataDTO> blockingQueue = new ArrayBlockingQueue<>(1);

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                // Subscribe
                session.subscribe("/topic/sensor-data", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return SensorDataDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        blockingQueue.offer((SensorDataDTO) payload);
                    }
                });

                // Send message
                SensorDataDTO sensorData = new SensorDataDTO();
                sensorData.setDeviceId("DEV001");
                sensorData.setTemperature(25.5);
                
                session.send("/app/sensor/update", sensorData);
            }
        };

        stompClient.connect(wsUrl, sessionHandler);

        // Wait for response
        SensorDataDTO received = blockingQueue.poll(5, TimeUnit.SECONDS);
        
        assertNotNull(received);
        assertEquals("DEV001", received.getDeviceId());
        assertEquals(25.5, received.getTemperature());
    }
}
```

---

## Best Practices

### 1. Connection Management

```java
// Client-side: Auto-reconnect
class WebSocketManager {
    private stompClient;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    private reconnectDelay = 3000;

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect(
            {},
            (frame) => {
                console.log('Connected');
                this.reconnectAttempts = 0;
                this.onConnected();
            },
            (error) => {
                console.error('Connection error:', error);
                this.handleReconnect();
            }
        );
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Reconnecting... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}
```

### 2. Message Handling

```java
// Server-side: Validate và sanitize messages
@MessageMapping("/sensor/update")
@SendTo("/topic/sensor-data")
public SensorDataDTO handleSensorUpdate(
    @Validated @Payload SensorDataDTO sensorData,
    Principal principal
) {
    // Validation
    if (sensorData.getTemperature() < -50 || sensorData.getTemperature() > 100) {
        throw new IllegalArgumentException("Temperature out of range");
    }
    
    // Security: Verify device ownership
    String username = principal.getName();
    if (!deviceService.isDeviceOwnedByUser(sensorData.getDeviceId(), username)) {
        throw new UnauthorizedException("Not authorized for this device");
    }
    
    // Process and save
    return sensorDataService.createSensorData(sensorData);
}
```

### 3. Error Handling

```java
// Global exception handler cho WebSocket
@MessageExceptionHandler
@SendToUser("/queue/errors")
public String handleException(Exception e) {
    return "Error: " + e.getMessage();
}

// Client-side error handling
stompClient.subscribe('/user/queue/errors', (message) => {
    console.error('Server error:', message.body);
    showErrorNotification(message.body);
});
```

### 4. Performance Optimization

```java
// Batching messages
@Scheduled(fixedDelay = 1000) // Every 1 second
public void sendBatchedUpdates() {
    List<SensorDataDTO> pendingUpdates = getPendingUpdates();
    
    if (!pendingUpdates.isEmpty()) {
        messagingTemplate.convertAndSend(
            "/topic/sensor-data/batch",
            pendingUpdates
        );
        clearPendingUpdates();
    }
}

// Throttling broadcasts
private final Map<String, Long> lastBroadcastTime = new ConcurrentHashMap<>();
private static final long BROADCAST_THROTTLE_MS = 100;

public void throttledBroadcast(String topic, Object message) {
    long now = System.currentTimeMillis();
    Long lastTime = lastBroadcastTime.getOrDefault(topic, 0L);
    
    if (now - lastTime >= BROADCAST_THROTTLE_MS) {
        messagingTemplate.convertAndSend(topic, message);
        lastBroadcastTime.put(topic, now);
    }
}
```

### 5. Monitoring và Logging

```java
@Component
public class WebSocketMetrics {
    
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    
    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        int connections = activeConnections.incrementAndGet();
        log.info("WebSocket connected. Active connections: {}", connections);
    }
    
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        int connections = activeConnections.decrementAndGet();
        log.info("WebSocket disconnected. Active connections: {}", connections);
    }
    
    public void onMessageSent() {
        long messages = totalMessages.incrementAndGet();
        if (messages % 1000 == 0) {
            log.info("Total messages sent: {}", messages);
        }
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public long getTotalMessages() {
        return totalMessages.get();
    }
}
```

---

## Troubleshooting

### 1. Connection Issues

**Problem: Client không connect được**
```
Error: WebSocket connection failed
```

**Solutions:**
```java
// 1. Kiểm tra CORS configuration
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")  // Allow all origins
            .withSockJS();
}

// 2. Kiểm tra firewall/proxy
// Đảm bảo WebSocket port không bị block

// 3. Enable SockJS fallback
// SockJS tự động fallback sang polling nếu WebSocket fail
```

### 2. Authentication Errors

**Problem: 401 Unauthorized**
```
Forbidden: Access denied
```

**Solutions:**
```java
// 1. Kiểm tra JWT token
System.out.println("Token: " + token);
System.out.println("Valid: " + jwtTokenProvider.validateToken(token));

// 2. Disable CSRF cho WebSocket (nếu cần)
@Override
protected boolean sameOriginDisabled() {
    return true;
}

// 3. Check security configuration
.simpDestMatchers("/app/**").authenticated()
```

### 3. Message Not Received

**Problem: Subscribe nhưng không nhận message**

**Debugging:**
```java
// Server: Enable debug logging
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG

// Client: Enable STOMP debug
stompClient.debug = (str) => {
    console.log('STOMP:', str);
};

// Check destination matching
console.log('Subscribed to:', destination);
console.log('Message sent to:', actualDestination);
```

### 4. Memory Leaks

**Problem: Memory tăng liên tục**

**Solutions:**
```java
// 1. Unsubscribe khi không dùng
const subscription = stompClient.subscribe('/topic/data', callback);
// Later...
subscription.unsubscribe();

// 2. Disconnect khi leave page
window.addEventListener('beforeunload', () => {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
});

// 3. Server: Cleanup disconnected sessions
@EventListener
public void handleDisconnect(SessionDisconnectEvent event) {
    String sessionId = event.getSessionId();
    cleanupSession(sessionId);
}
```

### 5. High Latency

**Problem: Messages bị delay**

**Solutions:**
```java
// 1. Enable heartbeat
config.enableSimpleBroker("/topic", "/queue")
      .setHeartbeatValue(new long[]{10000, 10000});

// 2. Optimize message size
// Gửi chỉ data cần thiết, không gửi toàn bộ object

// 3. Use batching cho high-frequency updates
@Scheduled(fixedDelay = 100)
public void sendBatch() {
    // Batch multiple updates into one message
}

// 4. Consider using external broker (RabbitMQ) cho scale
```

### 6. Testing Tools

```bash
# Install wscat for command-line testing
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:8080/ws

# Send CONNECT frame
CONNECT
accept-version:1.2

^@

# Subscribe
SUBSCRIBE
id:sub-0
destination:/topic/sensor-data

^@
```

---

## Tài Liệu Tham Khảo

### Official Documentation
- [STOMP Protocol Specification](https://stomp.github.io/stomp-specification-1.2.html)
- [Spring WebSocket Reference](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Spring STOMP Support](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)

### Libraries
- [SockJS](https://github.com/sockjs/sockjs-client)
- [STOMP.js](https://github.com/stomp-js/stompjs)

### Tools
- [WebSocket King Client](https://websocketking.com/)
- [Postman WebSocket](https://www.postman.com/)
- [wscat CLI tool](https://github.com/websockets/wscat)

---

## Kết Luận

STOMP over WebSocket cung cấp một giải pháp mạnh mẽ và standardized cho real-time messaging trong ứng dụng web. Key takeaways:

1. **Simple but Powerful**: STOMP đơn giản hóa việc implement pub/sub messaging
2. **Flexible Routing**: Destination-based routing cho phép organize messages hiệu quả
3. **Security**: Tích hợp tốt với Spring Security cho authentication/authorization
4. **Scalable**: Có thể scale với external broker như RabbitMQ
5. **Cross-platform**: Client libraries cho nhiều platforms

Với IoT system, STOMP đặc biệt hữu ích cho:
- Real-time sensor data streaming
- Device command and control
- User notifications
- System monitoring dashboard
- Multi-device synchronization

Happy coding! 🚀
