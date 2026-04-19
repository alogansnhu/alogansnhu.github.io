package edu.snhu.cs499.exceptions;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException(String entityType, String entityId) {
    super(entityType + " with ID '" + entityId + "' was not found.");
  }
}
