package edu.snhu.cs499.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TaskTest {

  @Test
  void constructor_acceptsValidValuesIncludingPriorityAndDueDate() {
    LocalDateTime dueDate = LocalDateTime.of(2026, 4, 15, 10, 30);
    Task task = new Task("TASK1", "Finish Paper", "Complete the final draft", Task.Priority.HIGH, dueDate);

    assertEquals("TASK1", task.getTaskId());
    assertEquals("Finish Paper", task.getName());
    assertEquals("Complete the final draft", task.getDescription());
    assertEquals(Task.Priority.HIGH, task.getPriority());
    assertEquals(dueDate, task.getDueDate());
  }

  @Test
  void constructor_rejectsInvalidValues() {
    LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

    assertThrows(IllegalArgumentException.class,
        () -> new Task(null, "Name", "Description", Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("ABCDEFGHIJK", "Name", "Description", Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", null, "Description", Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", "This task name is too long", "Description", Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", "Name", null, Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", "Name", "This description is intentionally made longer than fifty characters total.",
            Task.Priority.MEDIUM, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", "Name", "Description", null, dueDate));
    assertThrows(IllegalArgumentException.class,
        () -> new Task("TASK1", "Name", "Description", Task.Priority.MEDIUM, null));
  }

  @Test
  void setters_updateMutableFieldsAndEnforceValidation() {
    Task task = new Task("TASK1", "Name", "Description", Task.Priority.LOW, LocalDateTime.now().plusDays(2));
    LocalDateTime newDueDate = LocalDateTime.now().plusDays(5);

    task.setName("Updated Name");
    task.setDescription("Updated description");
    task.setPriority(Task.Priority.CRITICAL);
    task.setDueDate(newDueDate);

    assertEquals("Updated Name", task.getName());
    assertEquals("Updated description", task.getDescription());
    assertEquals(Task.Priority.CRITICAL, task.getPriority());
    assertEquals(newDueDate, task.getDueDate());

    assertThrows(IllegalArgumentException.class, () -> task.setName(""));
    assertThrows(IllegalArgumentException.class, () -> task.setDescription(""));
    assertThrows(IllegalArgumentException.class, () -> task.setPriority(null));
    assertThrows(IllegalArgumentException.class, () -> task.setDueDate(null));
  }

  @Test
  void taskId_isImmutable() throws Exception {
    Field idField = Task.class.getDeclaredField("taskId");
    assertTrue(Modifier.isFinal(idField.getModifiers()));

    boolean hasSetter = java.util.Arrays.stream(Task.class.getDeclaredMethods())
        .anyMatch(method -> method.getName().equals("setTaskId"));
    assertFalse(hasSetter);
  }
}
