package edu.snhu.cs499.exceptions;

public class DuplicateEntityException extends RuntimeException {
  public DuplicateEntityException(String entityType, String entityId) {
    super(entityType + " with ID '" + entityId + "' already exists.");
  }
}
