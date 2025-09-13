package com.example.logservice.controller;

import com.example.logservice.model.LogEntry;
import com.example.logservice.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * LogController: REST endpoints for adding and reading logs.
 */
@RestController
@RequestMapping("/logs")
public class LogController {
    @Autowired
    private LogEntryRepository logEntryRepository;

    /**
     * Add a new log entry.
     * Example JSON: { "service": "user", "message": "User created" }
     */
    @PostMapping("/add")
    public LogEntry addLog(@RequestBody LogEntry logEntry) {
        logEntry.setTimestamp(LocalDateTime.now());
        return logEntryRepository.save(logEntry);
    }

    /**
     * Read all logs, or filter by service.
     */
    @GetMapping("/read")
    public List<LogEntry> readLogs(@RequestParam(required = false) String service) {
        if (service != null && !service.isEmpty()) {
            return logEntryRepository.findByService(service);
        }
        return logEntryRepository.findAll();
    }
}
