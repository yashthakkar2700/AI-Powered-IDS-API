package idsapi.com.example.idsapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idsapi.com.example.idsapi.dto.DetectionResult;
import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.repository.LogEntryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LogProcessorService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MlService mlService;
    private final LogEntryRepository logEntryRepository;
    private final AlertService alertService;
    private final UserService userService;

    public LogProcessorService(MlService mlService,
                               LogEntryRepository logEntryRepository,
                               AlertService alertService,
                               UserService userService) {
        this.mlService = mlService;
        this.logEntryRepository = logEntryRepository;
        this.alertService = alertService;
        this.userService = userService;
    }

    /**
     * Consumes JSON string messages from topic raw-logs.
     * Expected payload (example):
     * {
     *   "logId": 123, "batchId":"...", "userId":7, "source":"upload", "message":"...", "timestamp":"..."
     * }
     */
    @KafkaListener(topics = "raw-logs", groupId = "ids-consumer-group")
    public void handleRawLog(String jsonPayload) {
        try {
            JsonNode node = objectMapper.readTree(jsonPayload);

            Long logId = node.has("logId") && !node.get("logId").isNull() ? node.get("logId").asLong() : null;
            Long userId = node.has("userId") && !node.get("userId").isNull() ? node.get("userId").asLong() : null;
            String source = node.has("source") ? node.get("source").asText() : "kafka";
            String message = node.has("message") ? node.get("message").asText() : "";

            LogEntry entry = null;
            if (logId != null) {
                Optional<LogEntry> o = logEntryRepository.findById(logId);
                if (o.isPresent()) entry = o.get();
            }

            if (entry == null) {
                entry = new LogEntry();
                entry.setSource(source);
                entry.setMessage(message);
                entry.setBatchId(node.has("batchId") ? node.get("batchId").asText() : null);
                if (userId != null) {
                    // set user reference; use userService to fetch user
                    entry.setUser(userService.getUserById(userId));
                }
                entry = logEntryRepository.save(entry);
            }

            // Call ML
            DetectionResult dr = mlService.analyzeLog(message);
            entry.setProcessed(true);
            entry.setAnomalyScore(dr.getScore());
            entry.setDetectionReason(dr.getReason());
            entry.setDetectedSeverity(dr.getSeverity());
            entry.setProcessedAt(LocalDateTime.now());
            logEntryRepository.save(entry);

            if (dr.isAnomaly()) {
                Alert alert = new Alert();
                alert.setType("Intrusion");
                alert.setSeverity(dr.getSeverity());
                alert.setMessage(dr.getReason());
                alert.setCreatedAt(LocalDateTime.now());
                alertService.createAlert(alert, entry.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Consider sending to a dead-letter topic in production
        }
    }
}
