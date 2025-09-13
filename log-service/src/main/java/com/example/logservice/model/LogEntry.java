package com.example.logservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * LogEntry: Represents a log record stored in MongoDB.
 */
@Document(collection = "logs")
public class LogEntry {
    @Id
    private String id;
    private String service;
    private String message;
    private LocalDateTime timestamp;

    public LogEntry() {}
    public LogEntry(String service, String message, LocalDateTime timestamp) {
        this.service = service;
        this.message = message;
        this.timestamp = timestamp;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
