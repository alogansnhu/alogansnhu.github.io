package edu.snhu.cs320.tasks;

import java.util.Objects;

public final class Task {

    private final String taskId;     // immutable by design
    private String name;
    private String description;

    /**
     * Constructs a Task with required fields and length constraints.
     */
    public Task(String taskId, String name, String description) {
        this.taskId = validateId(taskId);
        this.name = validateName(name);
        this.description = validateDescription(description);
    }

    // Getters
    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters for updatable fields only
    public void setName(String name) {
        this.name = validateName(name);
    }

    public void setDescription(String description) {
        this.description = validateDescription(description);
    }

    // Validation helpers
    private static String validateId(String id) {
        if (id == null || id.length() > 10) {
            throw new IllegalArgumentException("taskId must be non-null and <= 10 characters.");
        }
        return id;
    }

    private static String validateName(String name) {
        if (name == null || name.length() > 20) {
            throw new IllegalArgumentException("name must be non-null and <= 20 characters.");
        }
        return name;
    }

    private static String validateDescription(String description) {
        if (description == null || description.length() > 50) {
            throw new IllegalArgumentException("description must be non-null and <= 50 characters.");
        }
        return description;
    }

    // Equality on ID; service enforces uniqueness across instances.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return taskId.equals(task.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "Task{id='" + taskId + "', name='" + name + "', description='" + description + "'}";
    }
}