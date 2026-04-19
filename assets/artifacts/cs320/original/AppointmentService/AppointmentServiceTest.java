package edu.snhu.cs320.appointments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for AppointmentService.
 */
public class AppointmentServiceTest {

    private static Date future() {
        return new Date(System.currentTimeMillis() + 5_000);
    }

    @Test
    void addAppointment_enforcesUniqueIds() {
        AppointmentService service = new AppointmentService();
        Appointment a1 = new Appointment("ID1", future(), "one");
        Appointment a2 = new Appointment("ID2", future(), "two");
        service.addAppointment(a1);
        service.addAppointment(a2);

        assertEquals(2, service.size());
        assertTrue(service.getAppointment("ID1").isPresent());
        assertTrue(service.getAppointment("ID2").isPresent());

        // Duplicate ID should fail
        Appointment dup = new Appointment("ID1", future(), "duplicate");
        assertThrows(IllegalArgumentException.class, () -> service.addAppointment(dup));
    }

    @Test
    void deleteAppointment_byId() {
        AppointmentService service = new AppointmentService();
        Appointment a1 = new Appointment("DEL1", future(), "one");
        service.addAppointment(a1);
        assertEquals(1, service.size());

        boolean deleted = service.deleteAppointment("DEL1");
        assertTrue(deleted);
        assertEquals(0, service.size());
        assertFalse(service.getAppointment("DEL1").isPresent());

        // Deleting non existent returns false
        assertFalse(service.deleteAppointment("NOPE"));
    }
}