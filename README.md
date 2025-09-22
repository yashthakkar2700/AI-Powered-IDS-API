# IDS-API


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

## **Flow Notes**
  #### User -> Multiple Logs (Uploaded/Live) -> a Log(Whole file/live stream) can have Multiple Alerts.


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