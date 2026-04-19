package edu.snhu.cs499.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class AppointmentTest {

  @Test
  void constructor_acceptsValidValuesIncludingDuration() {
    LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Appointment appointment = new Appointment("APT1", start, 60, "Capstone meeting");

    assertEquals("APT1", appointment.getAppointmentId());
    assertEquals(start, appointment.getStartTime());
    assertEquals(start.plusMinutes(60), appointment.getEndTime());
    assertEquals("Capstone meeting", appointment.getAppointmentDescription());
  }

  @Test
  void constructor_rejectsInvalidValues() {
    LocalDateTime future = LocalDateTime.now().plusDays(1);

    assertThrows(IllegalArgumentException.class, () -> new Appointment(null, future, 30, "Desc"));
    assertThrows(IllegalArgumentException.class, () -> new Appointment("ABCDEFGHIJK", future, 30, "Desc"));
    assertThrows(IllegalArgumentException.class, () -> new Appointment("APT1", null, 30, "Desc"));
    assertThrows(IllegalArgumentException.class,
        () -> new Appointment("APT1", LocalDateTime.now().minusHours(1), 30, "Desc"));
    assertThrows(IllegalArgumentException.class, () -> new Appointment("APT1", future, 0, "Desc"));
    assertThrows(IllegalArgumentException.class, () -> new Appointment("APT1", future, 30, null));
  }

  @Test
  void overlaps_returnsTrueOnlyForIntersectingAppointments() {
    LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    Appointment first = new Appointment("APT1", base, 60, "First");
    Appointment second = new Appointment("APT2", base.plusMinutes(30), 60, "Second");
    Appointment third = new Appointment("APT3", base.plusMinutes(60), 30, "Third");

    assertTrue(first.overlaps(second));
    assertFalse(first.overlaps(third));
  }

  @Test
  void setters_updateMutableFieldsAndEnforceValidation() {
    Appointment appointment = new Appointment("APT1", LocalDateTime.now().plusDays(1), 45, "Initial");
    LocalDateTime newStart = LocalDateTime.now().plusDays(2);

    appointment.setStartTime(newStart);
    appointment.setDurationMinutes(90);
    appointment.setAppointmentDescription("Updated");

    assertEquals(newStart, appointment.getStartTime());
    assertEquals(90, appointment.getDurationMinutes());
    assertEquals("Updated", appointment.getAppointmentDescription());

    assertThrows(IllegalArgumentException.class, () -> appointment.setDurationMinutes(-1));
    assertThrows(IllegalArgumentException.class, () -> appointment.setAppointmentDescription(""));
  }
}
