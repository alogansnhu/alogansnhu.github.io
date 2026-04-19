package edu.snhu.cs320.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TaskService {

    private final Map<String, Task> tasks = new HashMap<>();

    /** Returns an unmodifiable view of current tasks. */
    public Map<String, Task> viewAll() {
        return Collections.unmodifiableMap(tasks);
    }

    /** Convenience: create & add a Task using raw fields. */
    public Task addTask(String taskId, String name, String description) {
        Task t = new Task(taskId, name, description);
        return addTask(t);
    }

    /** Add an existing Task instance; enforces unique ID. */
    public Task addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null");
        }
        String id = task.getTaskId();
        if (tasks.containsKey(id)) {
            throw new IllegalArgumentException("Task ID already exists: " + id);
        }
        tasks.put(id, task);
        return task;
    }

    /** Delete by ID; throws if not found. */
    public void deleteTask(String taskId) {
        requireNonNullId(taskId);
        if (tasks.remove(taskId) == null) {
            throw new IllegalArgumentException("No task found with ID: " + taskId);
        }
    }

    /** Update both fields together. */
    public Task updateTask(String taskId, String newName, String newDescription) {
        Task t = getRequired(taskId);
        // setters validate lengths and non-null
        t.setName(newName);
        t.setDescription(newDescription);
        return t;
    }

    /** Update name only. */
    public Task updateTaskName(String taskId, String newName) {
        Task t = getRequired(taskId);
        t.setName(newName);
        return t;
    }

    /** Update description only. */
    public Task updateTaskDescription(String taskId, String newDescription) {
        Task t = getRequired(taskId);
        t.setDescription(newDescription);
        return t;
    }

    /** Helper for tests or diagnostics; returns null if absent. */
    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }

    private Task getRequired(String taskId) {
        requireNonNullId(taskId);
        Task t = tasks.get(taskId);
        if (t == null) {
            throw new IllegalArgumentException("No task found with ID: " + taskId);
        }
        return t;
    }

    private static void requireNonNullId(String taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId must not be null");
        }
    }
}