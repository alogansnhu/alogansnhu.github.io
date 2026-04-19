package edu.snhu.cs320.tasks;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskServiceTest {

    // Helpers
    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    @Test
    @DisplayName("addTask succeeds and stores retrievable Task")
    void addTaskSuccess() {
        TaskService svc = new TaskService();
        Task t = svc.addTask("ID1", "Name", "Desc");
        assertNotNull(t);
        assertEquals("ID1", t.getTaskId());
        assertEquals(t, svc.getTask("ID1"));
        assertEquals(1, svc.viewAll().size());
    }

    @Test
    @DisplayName("addTask prevents duplicate IDs")
    void addTaskDuplicateIdThrows() {
        TaskService svc = new TaskService();
        svc.addTask("ID1", "Name", "Desc");
        assertThrows(IllegalArgumentException.class, () -> svc.addTask("ID1", "Other", "Other"));
    }

    @Test
    @DisplayName("deleteTask removes existing task")
    void deleteTaskSuccess() {
        TaskService svc = new TaskService();
        svc.addTask("ID1", "Name", "Desc");
        svc.deleteTask("ID1");
        assertNull(svc.getTask("ID1"));
        assertTrue(svc.viewAll().isEmpty());
    }

    @Test
    @DisplayName("deleteTask throws for non-existent ID")
    void deleteTaskNonExistentThrows() {
        TaskService svc = new TaskService();
        assertThrows(IllegalArgumentException.class, () -> svc.deleteTask("NOPE"));
        assertThrows(IllegalArgumentException.class, () -> svc.deleteTask(null));
    }

    @Test
    @DisplayName("updateTaskName updates name and validates constraints")
    void updateTaskNameSuccessAndValidation() {
        TaskService svc = new TaskService();
        svc.addTask("ID1", "Old", "D");

        Task updated = svc.updateTaskName("ID1", "NewName");
        assertEquals("NewName", updated.getName());

        String tooLong = repeat('N', 21);
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskName("ID1", tooLong));
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskName("ID1", null));
    }

    @Test
    @DisplayName("updateTaskDescription updates description and validates constraints")
    void updateTaskDescriptionSuccessAndValidation() {
        TaskService svc = new TaskService();
        svc.addTask("ID1", "Name", "Old");

        Task updated = svc.updateTaskDescription("ID1", "NewDesc");
        assertEquals("NewDesc", updated.getDescription());

        String tooLong = repeat('D', 51);
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskDescription("ID1", tooLong));
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskDescription("ID1", null));
    }

    @Test
    @DisplayName("updateTask updates both fields and validates")
    void updateTaskBothFields() {
        TaskService svc = new TaskService();
        svc.addTask("ID1", "N", "D");

        Task updated = svc.updateTask("ID1", "New", "NewDesc");
        assertEquals("New", updated.getName());
        assertEquals("NewDesc", updated.getDescription());

        String badName = repeat('N', 21);
        assertThrows(IllegalArgumentException.class, () -> svc.updateTask("ID1", badName, "ok"));

        String badDesc = repeat('D', 51);
        assertThrows(IllegalArgumentException.class, () -> svc.updateTask("ID1", "ok", badDesc));
    }

    @Test
    @DisplayName("update operations throw for non-existent ID")
    void updateNonExistentThrows() {
        TaskService svc = new TaskService();
        assertThrows(IllegalArgumentException.class, () -> svc.updateTask("NOPE", "n", "d"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskName("NOPE", "n"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateTaskDescription("NOPE", "d"));
    }
}