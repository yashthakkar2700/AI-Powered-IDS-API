package idsapi.com.example.idsapi.service;

import idsapi.com.example.idsapi.dto.DetectionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class MlService {

    private final WebClient webClient;

    public MlService(@Value("${ml.url:http://localhost:8000}") String mlUrl) {
        this.webClient = WebClient.builder().baseUrl(mlUrl).build();
    }

    /**
     * Synchronously analyze a single log line using the ML microservice.
     * This blocks (safe for Phase1). Later you can make it async.
     */
    public DetectionResult analyzeLog(String message) {
        try {
            Mono<DetectionResult> resp = webClient.post()
                    .uri("/analyze")
                    .bodyValue(new java.util.HashMap<String, String>() {{ put("message", message); }})
                    .retrieve()
                    .bodyToMono(DetectionResult.class);

            DetectionResult result = resp.block();
            if (result == null) {
                return new DetectionResult(false, 0.0, "LOW", "no response");
            }
            return result;
        } catch (Exception ex) {
            // In case of ML service failure, fallback to "not anomaly" or a safe default.
            ex.printStackTrace();
            return new DetectionResult(false, 0.0, "LOW", "ml-service-failed: " + ex.getMessage());
        }
    }
}