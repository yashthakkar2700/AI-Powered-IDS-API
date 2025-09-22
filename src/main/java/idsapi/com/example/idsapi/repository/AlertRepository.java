package idsapi.com.example.idsapi.repository;

import idsapi.com.example.idsapi.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

}
