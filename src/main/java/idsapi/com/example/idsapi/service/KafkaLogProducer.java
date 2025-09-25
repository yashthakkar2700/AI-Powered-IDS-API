package idsapi.com.example.idsapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaLogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaLogProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void produceRawLog(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("raw-logs", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
