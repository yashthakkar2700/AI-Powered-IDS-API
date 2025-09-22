package idsapi.com.example.idsapi.controller;

import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.service.LogEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogEntryController {
    private final LogEntryService logEntryService;

    public LogEntryController(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    // Create log entry for a specific user
    @PostMapping("/user/{userId}")
    public ResponseEntity<LogEntry> createLogEntry(@PathVariable Long userId, @RequestBody LogEntry logEntry) {
        return ResponseEntity.ok(logEntryService.createLogEntry(logEntry, userId));
    }

    @GetMapping
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        return ResponseEntity.ok(logEntryService.getAllLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogEntry> getLogById(@PathVariable Long id) {
        return ResponseEntity.ok(logEntryService.getLogById(id));
    }

    // Get all logs created by a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LogEntry>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(logEntryService.getLogsByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLog(@PathVariable Long id) {
        logEntryService.deleteLog(id);
        return ResponseEntity.ok("Log deleted successfully");
    }

}
