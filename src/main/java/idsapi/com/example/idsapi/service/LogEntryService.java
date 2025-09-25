package idsapi.com.example.idsapi.service;

import org.springframework.stereotype.Service;
import idsapi.com.example.idsapi.model.LogEntry;
import idsapi.com.example.idsapi.model.User;
import idsapi.com.example.idsapi.repository.LogEntryRepository;
import idsapi.com.example.idsapi.repository.UserRepository;

import java.util.List;

@Service
public class LogEntryService {
    private final LogEntryRepository logEntryRepository;
    private final UserRepository userRepository;

    public LogEntryService(LogEntryRepository logEntryRepository, UserRepository userRepository) {
        this.logEntryRepository = logEntryRepository;
        this.userRepository = userRepository;
    }

    // Create log entry and link it to a user
    public LogEntry createLogEntry(LogEntry logEntry, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        logEntry.setUser(user);
        return logEntryRepository.save(logEntry);
    }

    public LogEntry save(LogEntry logEntry) {
        return logEntryRepository.save(logEntry);
    }

    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }

    public LogEntry getLogById(Long id) {
        return logEntryRepository.findById(id).orElse(null);
    }

    public void deleteLog(Long id) {
        logEntryRepository.deleteById(id);
    }

    // Get all logs for a specific user
    public List<LogEntry> getLogsByUser(Long userId) {
        return logEntryRepository.findByUserId(userId);
    }

}
