package idsapi.com.example.idsapi.repository;

import idsapi.com.example.idsapi.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogEntry, Long> {}