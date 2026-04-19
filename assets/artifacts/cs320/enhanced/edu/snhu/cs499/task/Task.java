package edu.snhu.cs499.task;

import java.time.LocalDateTime;

import edu.snhu.cs499.common.Identifiable;
import edu.snhu.cs499.common.ValidationUtils;

public class Task implements Identifiable<String> {
  public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  private final String taskId;
  private String name;
  private String description;
  private Priority priority;
  private LocalDateTime dueDate;

  public Task(String taskId, String name, String description) {
    this(taskId, name, description, Priority.MEDIUM, LocalDateTime.now().plusDays(1));
  }

  public Task(String taskId, String name, String description, Priority priority, LocalDateTime dueDate) {
    this.taskId = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(taskId, "Task ID"),
        10,
        "Task ID");
    this.name = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(name, "Task name"),
        20,
        "Task name");
    this.description = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(description, "Task description"),
        50,
        "Task description");
    this.priority = ValidationUtils.requireNotNull(priority, "Task priority");
    this.dueDate = ValidationUtils.requireNotNull(dueDate, "Task due date");
  }

  @Override
  public String getId() {
    return taskId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Priority getPriority() {
    return priority;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setName(String name) {
    this.name = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(name, "Task name"),
        20,
        "Task name");
  }

  public void setDescription(String description) {
    this.description = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(description, "Task description"),
        50,
        "Task description");
  }

  public void setPriority(Priority priority) {
    this.priority = ValidationUtils.requireNotNull(priority, "Task priority");
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = ValidationUtils.requireNotNull(dueDate, "Task due date");
  }
}
