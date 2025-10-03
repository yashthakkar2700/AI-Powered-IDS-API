package idsapi.com.example.idsapi.service;

import idsapi.com.example.idsapi.dto.DetectionResult;
import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.model.User;
import idsapi.com.example.idsapi.repository.AlertRepository;
import idsapi.com.example.idsapi.repository.LogEntryRepository;
import idsapi.com.example.idsapi.repository.UserRepository;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LiveLogService {

    private final MlService mlService;
    private final AlertRepository alertRepository;
    private final LogEntryRepository logEntryRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private volatile Tailer tailer;
    private volatile Thread tailerThread;

    private User tailingUser;

    public LiveLogService(MlService mlService,
                          AlertRepository alertRepository,
                          LogEntryRepository logEntryRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.mlService = mlService;
        this.alertRepository = alertRepository;
        this.logEntryRepository = logEntryRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public String startTailing(String filePath) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        this.tailingUser = userRepository.findByUsername(username).orElse(null);

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid log file path: " + filePath);
        }

        stopTailing(); // stop old one if running

        TailerListenerAdapter listener = new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                System.out.println("📜 New line from Tailer: " + line);
                try {
                    processLiveLog(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void endOfFileReached() {
                System.out.println("📌 EOF reached, waiting for new lines...");
            }

            @Override
            public void fileNotFound() {
                System.err.println("⚠️ File not found: " + file.getAbsolutePath());
            }

            @Override
            public void fileRotated() {
                System.out.println("🔄 File rotated: " + file.getAbsolutePath());
            }
        };

        // ⬇ important: false = start from beginning, true = from end
        tailer = new Tailer(file, listener, 1000, true, true);

        // ⬇ NOT daemon, so it won’t die immediately
        tailerThread = new Thread(tailer, "LiveLog-Tailer");
        tailerThread.start();

        return "Live log monitoring started for: " + filePath;
    }

    public String stopTailing() {
        if (tailer != null) {
            tailer.stop();
            tailer = null;
            tailerThread = null;
            return "🛑 Monitoring stopped.";
        }
        return "ℹ️ No active monitoring.";
    }

    public boolean isRunning() {
        return tailer != null && tailerThread != null && tailerThread.isAlive();
    }

    private void processLiveLog(String message) {
        System.out.println("New Line: " + message);

        // Always push to frontend (real-time feed)
        messagingTemplate.convertAndSend("/topic/logs", message);

        DetectionResult result = mlService.analyzeLog(message);

        if (result.isAnomaly()) {
            LogEntry logEntry = new LogEntry();
            logEntry.setMessage(message);
            logEntry.setSource("live");
            logEntry.setProcessed(true);
            logEntry.setAnomalyScore(result.getScore());
            logEntry.setDetectionReason(result.getReason());
            logEntry.setDetectedSeverity(result.getSeverity());
            logEntry.setProcessedAt(LocalDateTime.now());
            logEntry.setUser(this.tailingUser);
            logEntryRepository.save(logEntry);

            Alert alert = new Alert();
            alert.setType("Intrusion");
            alert.setSeverity(result.getSeverity());
            alert.setMessage(result.getReason());
            alert.setCreatedAt(LocalDateTime.now());
            alert.setLogEntry(logEntry);
            alertRepository.save(alert);

            System.out.println("🚨 ALERT: " + result.getReason() + " | " + message);

            // Also broadcast anomalies separately
            messagingTemplate.convertAndSend("/topic/alerts", alert);
        }
    }
}
