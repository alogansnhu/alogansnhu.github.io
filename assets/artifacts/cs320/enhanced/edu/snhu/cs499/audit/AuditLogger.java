package edu.snhu.cs499.audit;

import java.util.List;

public interface AuditLogger {
  void record(AuditEvent event);

  List<AuditEvent> getEvents();
}
