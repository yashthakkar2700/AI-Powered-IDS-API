package idsapi.com.example.idsapi.service;

import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.repository.AlertRepository;
import idsapi.com.example.idsapi.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final LogEntryRepository logEntryRepository;


    public AlertService(AlertRepository alertRepository, LogEntryRepository logEntryRepository) {
        this.alertRepository = alertRepository;
        this.logEntryRepository = logEntryRepository;
    }

    // Create alert and link it to a log entry
    public Alert createAlert(Alert alert, Long logEntryId) {
        LogEntry logEntry = logEntryRepository.findById(logEntryId)
                .orElseThrow(() -> new RuntimeException("LogEntry not found with id: " + logEntryId));
        alert.setLogEntry(logEntry);
        return alertRepository.save(alert);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }

    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    // Get all alerts for a specific log entry
    public List<Alert> getAlertsByLogEntry(Long logEntryId) {
        return alertRepository.findByLogEntryId(logEntryId);
    }
}
