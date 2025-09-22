package idsapi.com.example.idsapi.controller;

import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // ✅ Create alert for a specific log entry
    @PostMapping("/log/{logEntryId}")
    public ResponseEntity<Alert> createAlert(@PathVariable Long logEntryId, @RequestBody Alert alert) {
        return ResponseEntity.ok(alertService.createAlert(alert, logEntryId));
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    // Get alerts for a specific log entry
    @GetMapping("/log/{logEntryId}")
    public ResponseEntity<List<Alert>> getAlertsByLogEntry(@PathVariable Long logEntryId) {
        return ResponseEntity.ok(alertService.getAlertsByLogEntry(logEntryId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok("Alert deleted successfully");
    }
}
