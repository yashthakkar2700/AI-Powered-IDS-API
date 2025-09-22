package idsapi.com.example.idsapi.repository;

import idsapi.com.example.idsapi.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    List<LogEntry> findByUserId(Long userId);
}