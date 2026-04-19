package edu.snhu.cs499.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TaskServiceTest {

  @Test
  void createTask_storesTaskAndPreventsDuplicateIds() {
    TaskService service = new TaskService();
    Task first = new Task("TASK1", "Prepare", "Prepare draft", Task.Priority.HIGH,
        LocalDateTime.of(2026, 4, 5, 9, 0));
    Task second = new Task("TASK2", "Submit", "Submit final", Task.Priority.MEDIUM,
        LocalDateTime.of(2026, 4, 6, 12, 0));

    service.create(first);
    service.create(second);

    assertEquals(2, service.findAll().size());
    assertTrue(service.getById("TASK1").isPresent());
    assertThrows(IllegalArgumentException.class, () -> service.create(first));
  }

  @Test
  void getTasksByPriority_returnsMostUrgentTasksFirst() {
    TaskService service = new TaskService();
    service.createTask("TASK1", "Low", "Low priority", Task.Priority.LOW,
        LocalDateTime.of(2026, 4, 10, 9, 0));
    service.createTask("TASK2", "Critical", "Critical priority", Task.Priority.CRITICAL,
        LocalDateTime.of(2026, 4, 12, 9, 0));
    service.createTask("TASK3", "High", "Earlier due", Task.Priority.HIGH,
        LocalDateTime.of(2026, 4, 8, 9, 0));

    List<Task> ordered = service.getTasksByPriority();

    assertEquals("TASK2", ordered.get(0).getTaskId());
    assertEquals("TASK3", ordered.get(1).getTaskId());
    assertEquals("TASK1", ordered.get(2).getTaskId());
    assertEquals("TASK2", service.peekMostUrgentTask().orElseThrow().getTaskId());
  }

  @Test
  void updateTask_reordersPriorityIndexWhenValuesChange() {
    TaskService service = new TaskService();
    service.createTask("TASK1", "Write", "Write intro", Task.Priority.MEDIUM,
        LocalDateTime.of(2026, 4, 20, 8, 0));
    service.createTask("TASK2", "Edit", "Edit body", Task.Priority.HIGH,
        LocalDateTime.of(2026, 4, 18, 8, 0));

    service.updateTask("TASK1", "Write", "Write final", Task.Priority.CRITICAL,
        LocalDateTime.of(2026, 4, 17, 8, 0));

    List<Task> ordered = service.getTasksByPriority();
    assertEquals("TASK1", ordered.get(0).getTaskId());
    assertEquals("TASK2", ordered.get(1).getTaskId());
    assertEquals("Write final", service.getById("TASK1").orElseThrow().getDescription());
  }

  @Test
  void getTasksDueBefore_filtersTasksByDeadline() {
    TaskService service = new TaskService();
    service.createTask("TASK1", "Soon", "Soon task", Task.Priority.MEDIUM,
        LocalDateTime.of(2026, 4, 10, 8, 0));
    service.createTask("TASK2", "Later", "Later task", Task.Priority.HIGH,
        LocalDateTime.of(2026, 4, 20, 8, 0));

    List<Task> dueSoon = service.getTasksDueBefore(LocalDateTime.of(2026, 4, 12, 0, 0));

    assertEquals(1, dueSoon.size());
    assertEquals("TASK1", dueSoon.get(0).getTaskId());
  }

  @Test
  void deleteTask_removesTaskFromAllStructures() {
    TaskService service = new TaskService();
    service.createTask("TASK1", "Delete", "Delete task", Task.Priority.LOW,
        LocalDateTime.of(2026, 4, 22, 8, 0));

    assertTrue(service.deleteById("TASK1"));
    assertFalse(service.getById("TASK1").isPresent());
    assertEquals(0, service.findAll().size());
    assertTrue(service.getTasksByPriority().isEmpty());
  }

  @Test
  void updateTask_throwsForUnknownTaskId() {
    TaskService service = new TaskService();
    assertThrows(IllegalArgumentException.class,
        () -> service.updateTask("MISSING", "Name", "Description", Task.Priority.HIGH,
            LocalDateTime.of(2026, 4, 1, 8, 0)));
  }
}
