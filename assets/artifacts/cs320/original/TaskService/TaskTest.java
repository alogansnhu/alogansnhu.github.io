package edu.snhu.cs320.tasks;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskTest {

    // Helpers
    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    @Test
    @DisplayName("Construct valid Task within all max lengths")
    void constructValidTask() {
        String id = repeat('A', 10);          // max allowed
        String name = repeat('N', 20);        // max allowed
        String desc = repeat('D', 50);        // max allowed

        Task t = new Task(id, name, desc);
        assertEquals(id, t.getTaskId());
        assertEquals(name, t.getName());
        assertEquals(desc, t.getDescription());
    }

    @Test
    @DisplayName("taskId must be non-null and <= 10")
    void invalidIdNullOrTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new Task(null, "ok", "ok"));
        String tooLongId = repeat('X', 11);
        assertThrows(IllegalArgumentException.class, () -> new Task(tooLongId, "ok", "ok"));
    }

    @Test
    @DisplayName("name must be non-null and <= 20")
    void invalidNameNullOrTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new Task("ID", null, "ok"));
        String tooLongName = repeat('N', 21);
        assertThrows(IllegalArgumentException.class, () -> new Task("ID", tooLongName, "ok"));
    }

    @Test
    @DisplayName("description must be non-null and <= 50")
    void invalidDescriptionNullOrTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new Task("ID", "ok", null));
        String tooLongDesc = repeat('D', 51);
        assertThrows(IllegalArgumentException.class, () -> new Task("ID", "ok", tooLongDesc));
    }

    @Test
    @DisplayName("setName enforces constraints and updates value")
    void setNameValidAndInvalid() {
        Task t = new Task("ID", "old", "desc");
        t.setName("new");
        assertEquals("new", t.getName());

        String tooLongName = repeat('N', 21);
        assertThrows(IllegalArgumentException.class, () -> t.setName(tooLongName));
        assertThrows(IllegalArgumentException.class, () -> t.setName(null));
    }

    @Test
    @DisplayName("setDescription enforces constraints and updates value")
    void setDescriptionValidAndInvalid() {
        Task t = new Task("ID", "name", "old");
        t.setDescription("new");
        assertEquals("new", t.getDescription());

        String tooLongDesc = repeat('D', 51);
        assertThrows(IllegalArgumentException.class, () -> t.setDescription(tooLongDesc));
        assertThrows(IllegalArgumentException.class, () -> t.setDescription(null));
    }

    @Test
    @DisplayName("taskId is immutable (final field, no setter)")
    void taskIdIsImmutable() throws Exception {
        Field idField = Task.class.getDeclaredField("taskId");
        assertTrue(Modifier.isFinal(idField.getModifiers()), "taskId must be final");
        // Also verify no setTaskId method exists
        boolean hasSetter = java.util.Arrays.stream(Task.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("setTaskId"));
        assertFalse(hasSetter, "Task must not expose a setter for taskId");
    }
}