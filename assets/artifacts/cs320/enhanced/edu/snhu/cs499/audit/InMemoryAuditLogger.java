package edu.snhu.cs499.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryAuditLogger implements AuditLogger {
  private final List<AuditEvent> events = new ArrayList<>();

  @Override
  public void record(AuditEvent event) {
    events.add(event);
  }

  @Override
  public List<AuditEvent> getEvents() {
    return Collections.unmodifiableList(new ArrayList<>(events));
  }
}
