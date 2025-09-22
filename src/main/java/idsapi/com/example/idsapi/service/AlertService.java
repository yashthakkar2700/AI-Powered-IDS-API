package idsapi.com.example.idsapi.service;

import idsapi.com.example.idsapi.model.Alert;
import idsapi.com.example.idsapi.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }

    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }
}
