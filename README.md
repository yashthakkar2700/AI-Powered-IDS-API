# IDS-API


## Steps/notes to run Project:
- start Xampp
- **Run** IdsapiApplication.java (main file) from Intellij right-click run
- Open docker desktop app from start menu
- Open powerShell on 'docker-compose.yml' level:
````D:\programming\web develpement\Backend\Java-full-stack\IDS````
- Command to run **docker** (for Zookeeper, kafka, ML-api):
````docker-compose up --build -d````
- ##### Refer docker notes (scroll down) for more.

---

## **Authentication Routes (`/auth`)**

| Method | Endpoint           | Description                     | Auth Required |
|--------|------------------|---------------------------------|---------------|
| POST   | `/auth/register`  | Register a new user             | ❌ No         |
| POST   | `/auth/login`     | Login and get JWT token         | ❌ No         |
| GET    | `/auth/profile`   | Get user profile info           | ✅ Yes (JWT)  |

**Headers for `/profile`:**
Authorization: Bearer <JWT_TOKEN>


---

## **User Routes (`/users`)**

| Method | Endpoint           | Description                     | Auth Required |
|--------|------------------|---------------------------------|---------------|
| POST   | `/users`          | Create new user                 | ✅ Yes        |
| GET    | `/users`          | Get all users                   | ✅ Yes        |
| GET    | `/users/{id}`     | Get user by ID                  | ✅ Yes        |
| DELETE | `/users/{id}`     | Delete user by ID               | ✅ Yes        |

---

## **LogEntry Routes (`/logs`)**

| Method | Endpoint              | Description                                  | Auth Required |
|--------|-----------------------|----------------------------------------------|---------------|
| POST   | `/logs?userId={id}`   | Create new log entry for a user              | ✅ Yes        |
| GET    | `/logs`               | Get all log entries                          | ✅ Yes        |
| GET    | `/logs/{id}`          | Get log entry by ID                          | ✅ Yes        |
| DELETE | `/logs/{id}`          | Delete log entry by ID                       | ✅ Yes        |

**LogEntry Model Fields:**
- `id` (Long)
- `source` (String: "upload" / "live-stream")
- `message` (String)
- `timestamp` (LocalDateTime)
- `user` (User)

---

## **Alert Routes (`/alerts`)**

| Method | Endpoint        | Description             | Auth Required |
|--------|-----------------|-------------------------|---------------|
| POST   | `/alerts`       | Create a new alert      | ✅ Yes        |
| GET    | `/alerts`       | Get all alerts          | ✅ Yes        |
| GET    | `/alerts/{id}`  | Get alert by ID         | ✅ Yes        |
| DELETE | `/alerts/{id}`  | Delete alert by ID      | ✅ Yes        |

**Alert Model Fields:**
- `id` (Long)
- `message` (String)
- `severity` (String: HIGH / MEDIUM / LOW)
- `timestamp` (LocalDateTime)
- `logEntry` (LogEntry)

---

## **Log Upload & ML Detection Routes (`/logs`)**
| Method | Endpoint                  | Description                                         | Auth Required |
|--------|---------------------------|-----------------------------------------------------|---------------|
| POST   | `/logs/upload`            | Upload a log file; each line is processed via ML API and saved as LogEntry. Alerts are generated if anomalies are detected. | ✅ Yes        |

**Request Parameters:**
- `file` (MultipartFile): The log file to upload
- `userId` (Long): ID of the user uploading the file

**Response:**
```json
{
  "batchId": "generated-batch-uuid",
  "alertsCreated": 2,
  "logIds": [101, 102, 103]
}
```

## **Flow Notes**
  #### User -> Multiple Logs (Uploaded/Live) -> a Log(Whole file/live stream) can have Multiple Alerts.
  #### Upload log → save in DB → produce to Kafka → run ML detection → generate alerts → save processed logs.

## **JWT Notes**
- All protected routes require **Authorization header**: 
- Authorization: Bearer <JWT_TOKEN>

- Use `/auth/login` to obtain the token.

---

## **Testing**
- Include JWT token in headers for all protected routes.

---

## **Dependencies**
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL
- JWT (jjwt)
- Lombok

---
## **1. Docker Setup**

We containerized parts of the project for easier management and reproducibility.

### **Docker Containers Used**
- **ML API**: FastAPI service for AI-powered log detection
- **Kafka**: Message broker for log streaming
- **Zookeeper**: Required by Kafka for coordination

### **Steps to Run Docker**
1. Navigate to project root: ```cd "D:\programming\web develpement\Backend\Java-full-stack\IDS"```

2. Build and start containers: ```docker-compose up --build -d```
3. Verify containers: ````docker ps```` (You should see ml-api, kafka, and zookeeper running.)
4. If you wanna Check logs: 
````
docker logs ml-api
docker logs kafka
docker logs zookeeper
````
## **Kafka Usage**

Kafka is used to stream log data between the Spring Boot IDS backend and ML API for real-time anomaly detection.

### **Topics**
- `raw_logs`: All uploaded or streamed logs are published here.
- `alerts`: Optional future topic for processed alerts.

### **Commands**

#### List Topics
```bash
docker exec -it kafka kafka-topics.sh --bootstrap-server kafka:9092 --list
```
#### Create Topic
```
docker exec -it kafka kafka-topics.sh --bootstrap-server kafka:9092 --create --topic raw_logs --partitions 1 --replication-factor 1
```

#### Produce Message
```
docker exec -it kafka kafka-console-producer.sh --bootstrap-server kafka:9092 --topic raw_logs
```
Type any message and hit Enter.

#### Consume Message
```
docker exec -it kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic raw_logs --from-beginning
```
## **ML API (FastAPI)**

The ML API performs real-time log analysis and anomaly detection.

## **Project Flow: Log Processing & AI Detection**

This section explains how the system processes logs after a file upload and how the ML API integrates with the backend.

---

### **1. Upload Log File**
- Endpoint: `POST /api/logs/upload`
- Parameters:
    - `file`: Log file (each line is a separate log)
    - `userId`: Owner of the logs
- Process:
    1. Spring Boot controller receives the uploaded file.
    2. Each line is validated and prepared for processing.
    3. A unique `batchId` is generated for the uploaded file.

---

### **2. Persist Logs**
- Each log line is saved in the `LogEntry` table with fields:
    - `source`: "upload" or "live"
    - `message`: Raw log line
    - `user`: Associated user
    - `batchId`: Upload batch identifier
- Initially, logs are marked as `unprocessed`.

---

### **3. Kafka Integration**
- Each log line is published to Kafka `raw_logs` topic.
- Payload example:
```json
{
  "logId": 123,
  "batchId": "abc-uuid-123",
  "userId": 1,
  "source": "upload",
  "message": "Failed login attempt"
}
```
---

### **4. ML API Analysis**
- Each log line is sent **synchronously** to the ML API for anomaly detection.
- ML API Endpoint: `POST /analyze`
- Request Body Example:
```json
{
  "log": "Failed login attempt from IP 192.168.1.1"
}
```
- Response example:
```json
{
  "anomaly": true,
  "score": 0.95,
  "severity": "HIGH",
  "reason": "Multiple failed login attempts detected"
}
```
ML API uses AI models to detect anomalies in logs, returning:

- anomaly → true/false 
- score → anomaly confidence 
- severity → HIGH / MEDIUM / LOW 
- reason → explanation for detection

---

### **5. Post-Processing**
- Based on ML API results:
    - If `anomaly = true`:
        - Update `LogEntry`:
            - `processed = true`
            - `anomalyScore = 0.95`
            - `detectionReason = "Multiple failed login attempts detected"`
            - `detectedSeverity = HIGH`
            - `processedAt = current timestamp`
        - Create a corresponding `Alert` linked to this `LogEntry`:
            - `type = "Intrusion"`
            - `severity = HIGH`
            - `message = detection reason`
            - `createdAt = current timestamp`
    - If `anomaly = false`:
        - Update `LogEntry` with `processed = true` and anomaly score (optional).

---

### **6. Alert Creation**
- Alerts are stored in the `alerts` table.
- Alerts are connected to:
    - The user who uploaded the log
    - The `LogEntry` from which the anomaly was detected
- This allows tracking and historical analysis of anomalies.

---

### **7. Optional: Asynchronous Consumers**
- Other microservices can subscribe to Kafka topics:
    - `raw_logs` for unprocessed logs
    - `alerts` for anomaly events
- Enables scaling and real-time monitoring.

---

### **8. Summary Flow Diagram (Textual)**
User Uploads File --> Spring Boot Controller
````
│
▼
Split file into log lines
│
▼
Persist LogEntry in DB
│
▼
Produce log line to Kafka "raw_logs"
│
▼
Call ML API for detection
│
├─ If anomaly → Create Alert, Update LogEntry
│
└─ If normal → Mark LogEntry processed
````