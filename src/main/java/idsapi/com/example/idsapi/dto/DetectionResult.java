package idsapi.com.example.idsapi.dto;

public class DetectionResult {
    private boolean anomaly;
    private double score;
    private String severity;
    private String reason;

    public DetectionResult() {}

    public DetectionResult(boolean anomaly, double score, String severity, String reason) {
        this.anomaly = anomaly;
        this.score = score;
        this.severity = severity;
        this.reason = reason;
    }

    public boolean isAnomaly() { return anomaly; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}