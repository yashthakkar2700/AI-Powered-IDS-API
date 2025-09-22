package com.example.ids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@SpringBootApplication
public class IdsApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdsApplication.class, args);
    }
}

// ---------------- AUTH CONTROLLER ----------------
@RestController
@RequestMapping("/api/auth")
class AuthController {
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> user) {
        return "User registered: " + user.get("username");
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) {
        return "JWT-TOKEN-SAMPLE";
    }

    @GetMapping("/me")
    public Map<String, String> me() {
        return Map.of("username", "testUser", "role", "admin");
    }
}

// ---------------- LOGS CONTROLLER ----------------
@RestController
@RequestMapping("/api/logs")
class LogsController {
    private List<Map<String, Object>> logs = new ArrayList<>();

    @PostMapping("/upload")
    public String upload(@RequestBody Map<String, Object> log) {
        logs.add(log);
        return "Log uploaded";
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return logs;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        return logs.get(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        logs.remove(id);
        return "Log deleted";
    }
}

// ---------------- DETECTION CONTROLLER ----------------
@RestController
@RequestMapping("/api/detect")
class DetectionController {
    @PostMapping
    public Map<String, Object> detect(@RequestBody Map<String, Object> data) {
        // Mock AI detection
        return Map.of(
                "status", "intrusion_detected",
                "confidence", 0.92,
                "details", data
        );
    }

    @GetMapping
    public List<Map<String, Object>> getAllDetections() {
        return List.of(Map.of("id", 1, "status", "normal"), Map.of("id", 2, "status", "intrusion"));
    }
}

// ---------------- ALERTS CONTROLLER ----------------
@RestController
@RequestMapping("/api/alerts")
class AlertsController {
    private List<Map<String, Object>> alerts = new ArrayList<>();

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return alerts;
    }

    @PostMapping("/test")
    public String testAlert() {
        alerts.add(Map.of("id", alerts.size() + 1, "msg", "Test intrusion alert!"));
        return "Test alert generated";
    }

    @PutMapping("/{id}/ack")
    public String ack(@PathVariable int id) {
        return "Alert " + id + " acknowledged";
    }
}

// ---------------- DASHBOARD CONTROLLER ----------------
@RestController
@RequestMapping("/api/dashboard")
class DashboardController {
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of("totalLogs", 150, "intrusions", 20, "normalTraffic", 130);
    }

    @GetMapping("/trends")
    public List<Map<String, Object>> trends() {
        return List.of(
                Map.of("date", "2025-09-01", "intrusions", 5),
                Map.of("date", "2025-09-02", "intrusions", 10)
        );
    }
}
