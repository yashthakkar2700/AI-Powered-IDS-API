package idsapi.com.example.idsapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;      // e.g., "Intrusion"

    @Column(nullable = false)
    private String severity;  // HIGH / MEDIUM / LOW

    @Column(nullable = false)
    private String message;   // extra info

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
