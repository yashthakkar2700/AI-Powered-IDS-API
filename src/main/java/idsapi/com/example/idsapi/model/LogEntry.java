package idsapi.com.example.idsapi.model;

import jakarta.persistence.*;
import lombok.*;

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

    private String source;   // e.g. "upload" or "live"
    private String message;  // raw log line

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "logEntry", cascade = CascadeType.ALL)
    private List<Alert> alerts;

    public void setUser(User user) {
        this.user = user;
    }
}
