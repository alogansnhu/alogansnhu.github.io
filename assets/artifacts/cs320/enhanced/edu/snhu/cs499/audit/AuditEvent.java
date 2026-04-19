package edu.snhu.cs499.audit;

import java.time.LocalDateTime;

public class AuditEvent {
  private final LocalDateTime timestamp;
  private final AuditAction action;
  private final String entityType;
  private final String entityId;
  private final String message;

  public AuditEvent(LocalDateTime timestamp, AuditAction action, String entityType, String entityId, String message) {
    this.timestamp = timestamp;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.message = message;
  }

  public static AuditEvent now(AuditAction action, String entityType, String entityId, String message) {
    return new AuditEvent(LocalDateTime.now(), action, entityType, entityId, message);
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public AuditAction getAction() {
    return action;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getMessage() {
    return message;
  }
}
