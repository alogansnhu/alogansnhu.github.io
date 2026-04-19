package edu.snhu.cs320.appointments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for Appointment class.
 * Uses java.util.Date
 */
public class AppointmentTest {

    @Test
    void constructor_acceptsValidValues_professorStyle() {
        // future date relative to now
        Date currentDate = new Date(System.currentTimeMillis());
        Date afterToday = new Date(currentDate.getTime() + 1_000);  // small future offset for reliability
        Appointment appointment = new Appointment("12345", afterToday, "Description");

        assertTrue(appointment.getAppointmentDate().compareTo(afterToday) == 0);
        assertTrue(appointment.getAppointmentId().equals("12345"));
        assertTrue(appointment.getAppointmentDescription().equals("Description"));
    }

    @Test
    void id_mustNotBeNull_orEmpty_andMax10() {
        Date future = new Date(System.currentTimeMillis() + 1_000);
        assertThrows(IllegalArgumentException.class, () -> new Appointment(null, future, "ok"));
        assertThrows(IllegalArgumentException.class, () -> new Appointment("", future, "ok"));
        assertThrows(IllegalArgumentException.class, () -> new Appointment("ABCDEFGHIJK", future, "ok")); // 11 chars

        // boundary OK: 10 chars
        Appointment a = new Appointment("ABCDEFGHIJ", future, "ok");
        assertEquals("ABCDEFGHIJ", a.getAppointmentId());
    }

    @Test
    void date_mustNotBeNull_orPast() {
        Date now = new Date();
        assertThrows(IllegalArgumentException.class, () -> new Appointment("A1", null, "desc"));

        // past date
        Date past = new Date(now.getTime() - 1_000);
        assertThrows(IllegalArgumentException.class, () -> new Appointment("A1", past, "desc"));

        // future date OK
        Date future = new Date(now.getTime() + 1_000);
        Appointment a = new Appointment("A1", future, "desc");
        assertEquals(future, a.getAppointmentDate());
    }

    @Test
    void description_constraints_followSpec() {
        Date future = new Date(System.currentTimeMillis() + 1_000);
        assertThrows(IllegalArgumentException.class, () -> new Appointment("A1", future, null));
        
        // 51 chars -> too long
        String tooLong = "123456789012345678901234567890123456789012345678901"; // 51
        assertThrows(IllegalArgumentException.class, () -> new Appointment("A1", future, tooLong));

        // boundary OK: 50 chars
        String fifty = "12345678901234567890123456789012345678901234567890"; // 50
        Appointment a1 = new Appointment("A1", future, fifty);
        assertEquals(fifty, a1.getAppointmentDescription());
        
        // empty string
        Appointment a2 = new Appointment("A2", future, "");
        assertEquals("", a2.getAppointmentDescription());
    }

    @Test
    void appointmentId_isNotUpdatable_noSetterPresent() {
        // reflective lookup could fail because there is no setAppointmentId(String)
        assertThrows(NoSuchMethodException.class, () ->
                Appointment.class.getDeclaredMethod("setAppointmentId", String.class));
    }

    @Test
    void setters_enforceSameRules() {
        Date future = new Date(System.currentTimeMillis() + 5_000);
        Appointment a = new Appointment("SET1", future, "initial");

        // Update to another valid future date
        Date future2 = new Date(System.currentTimeMillis() + 10_000);
        a.setAppointmentDate(future2);
        assertEquals(future2, a.getAppointmentDate());

        // Past update should fail
        Date past = new Date(System.currentTimeMillis() - 1_000);
        assertThrows(IllegalArgumentException.class, () -> a.setAppointmentDate(past));

        // Description updates
        a.setAppointmentDescription("updated");
        assertEquals("updated", a.getAppointmentDescription());
        String tooLong = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // 51
        assertThrows(IllegalArgumentException.class, () -> a.setAppointmentDescription(tooLong));
    }
}