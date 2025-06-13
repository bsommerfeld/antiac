package de.bsommerfeld.antiac.hibernate.repository;

import de.bsommerfeld.antiac.hibernate.entity.LogEntry;
import java.util.List;

public interface LogEntryRepository {
  void save(LogEntry logEntry);

  LogEntry findById(long id);

  List<LogEntry> findAll();

  void delete(LogEntry logEntry);
}
