package idsapi.com.example.idsapi.controller;

import idsapi.com.example.idsapi.service.LiveLogService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/live")
public class LiveLogController {

    private final LiveLogService liveLogService;

    public LiveLogController(LiveLogService liveLogService) {
        this.liveLogService = liveLogService;
    }

    /**
     * Start live log monitoring
     * Example Request (Postman JSON body):
     * {
     *   "path": "D:\\postman-test-logs.txt"
     * }
     */
    @PostMapping("/start")
    public String startTailing(@RequestBody Map<String, Object> req) {
        String path = (String) req.get("path");
        return liveLogService.startTailing(path);
    }

    /**
     * Stop monitoring
     */
    @PostMapping("/stop")
    public String stop() {
        return liveLogService.stopTailing();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "active", liveLogService.isRunning()
        );
    }
}
