package idsapi.com.example.idsapi.controller;

import idsapi.com.example.idsapi.dto.DetectionResult;
import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.service.AlertService;
import idsapi.com.example.idsapi.service.KafkaLogProducer;
import idsapi.com.example.idsapi.service.LogEntryService;
import idsapi.com.example.idsapi.service.MlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/logs")
public class LogUploadController {

    private final LogEntryService logEntryService;
    private final MlService mlService;
    private final AlertService alertService;
    private final KafkaLogProducer kafkaLogProducer;

    public LogUploadController(LogEntryService logEntryService,
                               MlService mlService,
                               AlertService alertService,
                               KafkaLogProducer kafkaLogProducer) {
        this.logEntryService = logEntryService;
        this.mlService = mlService;
        this.alertService = alertService;
        this.kafkaLogProducer = kafkaLogProducer;
    }

    /**
     * Upload a log file. Each line will be saved as a LogEntry and processed.
     * Query param userId is required (owner of the upload).
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadLogFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("userId") Long userId) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        String batchId = UUID.randomUUID().toString();
        int processedCount = 0;
        List<Long> createdLogIds = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Persist log entry and link to user
                LogEntry entry = new LogEntry();
                entry.setSource("upload");
                entry.setMessage(line);
                entry.setBatchId(batchId);
                entry = logEntryService.createLogEntry(entry, userId); // creates and returns saved entity

                // Produce to Kafka (so async consumers can also see it)
                Map<String, Object> payload = new HashMap<>();
                payload.put("logId", entry.getId());
                payload.put("batchId", batchId);
                payload.put("userId", userId);
                payload.put("source", "upload");
                payload.put("message", line);
                kafkaLogProducer.produceRawLog(payload);

                // For Phase1: call ML synchronously to quickly get results
                DetectionResult dr = mlService.analyzeLog(line);
                if (dr != null && dr.isAnomaly()) {
                    entry.setProcessed(true);
                    entry.setAnomalyScore(dr.getScore());
                    entry.setDetectionReason(dr.getReason());
                    entry.setDetectedSeverity(dr.getSeverity());
                    entry.setProcessedAt(LocalDateTime.now());
                    logEntryService.save(entry);

                    // create alert linked to this log entry
                    Alert alert = new Alert();
                    alert.setType("Intrusion");
                    alert.setSeverity(dr.getSeverity());
                    alert.setMessage(dr.getReason());
                    alert.setCreatedAt(LocalDateTime.now());
                    alertService.createAlert(alert, entry.getId());
                    processedCount++;
                    createdLogIds.add(entry.getId());
                } else {
                    // Even if not anomaly, mark processed fields (optionally)
                    entry.setProcessed(true);
                    entry.setAnomalyScore(dr != null ? dr.getScore() : 0.0);
                    entry.setProcessedAt(LocalDateTime.now());
                    logEntryService.save(entry);
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("batchId", batchId);
            resp.put("alertsCreated", processedCount);
            resp.put("logIds", createdLogIds);
            return ResponseEntity.ok(resp);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to process file: " + ex.getMessage());
        }
    }
}
