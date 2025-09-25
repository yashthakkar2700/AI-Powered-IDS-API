package idsapi.com.example.idsapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;   // "upload" or "live"

    @Column(nullable = false, length = 1000)
    private String message;  // raw log line or content

    // Optional batch id for group uploads
    private String batchId;

    // ML processing info
    private boolean processed = false;
    private double anomalyScore = 0.0;
    private String detectionReason;
    private String detectedSeverity;
    private LocalDateTime processedAt;

    // Link log entry to the user who uploaded it
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One log entry can have multiple alerts
    @OneToMany(mappedBy = "logEntry", cascade = CascadeType.ALL)
    private List<Alert> alerts;
}