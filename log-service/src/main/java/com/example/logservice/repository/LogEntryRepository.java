package com.example.logservice.repository;

import com.example.logservice.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * LogEntryRepository: MongoDB repository for LogEntry documents.
 */
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByService(String service);
}
