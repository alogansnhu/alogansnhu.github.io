package edu.snhu.cs499.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.snhu.cs499.exceptions.SchedulingConflictException;

public class AppointmentServiceTest {

  @Test
  void scheduleAppointment_storesAppointmentsInChronologicalOrder() {
    AppointmentService service = new AppointmentService();
    LocalDateTime day = LocalDateTime.now().plusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0);

    Appointment later = new Appointment("APT2", day.withHour(14), 30, "Later");
    Appointment earlier = new Appointment("APT1", day.withHour(9), 30, "Earlier");

    service.create(later);
    service.create(earlier);

    List<Appointment> appointments = service.getAppointmentsForDay(day.toLocalDate());
    assertEquals(2, appointments.size());
    assertEquals("APT1", appointments.get(0).getAppointmentId());
    assertEquals("APT2", appointments.get(1).getAppointmentId());
  }

  @Test
  void addAppointment_detectsSchedulingConflicts() {
    AppointmentService service = new AppointmentService();
    LocalDateTime day = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

    service.scheduleAppointment("APT1", day, 60, "First");

    Appointment conflicting = new Appointment("APT2", day.plusMinutes(30), 30, "Conflict");
    assertThrows(SchedulingConflictException.class, () -> service.create(conflicting));
  }

  @Test
  void getAppointmentsInRange_returnsOnlyRequestedDays() {
    AppointmentService service = new AppointmentService();
    LocalDateTime base = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);

    service.scheduleAppointment("APT1", base, 30, "Day one");
    service.scheduleAppointment("APT2", base.plusDays(1), 30, "Day two");
    service.scheduleAppointment("APT3", base.plusDays(4), 30, "Day five");

    List<Appointment> inRange = service.getAppointmentsInRange(base.toLocalDate(), base.plusDays(1).toLocalDate());

    assertEquals(2, inRange.size());
    assertEquals("APT1", inRange.get(0).getAppointmentId());
    assertEquals("APT2", inRange.get(1).getAppointmentId());
  }

  @Test
  void deleteAppointment_removesAppointmentFromIndexes() {
    AppointmentService service = new AppointmentService();
    LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0);
    service.scheduleAppointment("APT1", start, 45, "Delete me");

    assertEquals(true, service.deleteById("APT1"));
    assertEquals(false, service.getById("APT1").isPresent());
    assertEquals(true, service.getAppointmentsForDay(start.toLocalDate()).isEmpty());
  }

  @Test
  void rescheduleAppointment_revalidatesConflictsAndOrdering() {
    AppointmentService service = new AppointmentService();
    LocalDateTime day = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0);

    service.scheduleAppointment("APT1", day, 60, "Morning");
    service.scheduleAppointment("APT2", day.plusHours(2), 60, "Noon");

    service.rescheduleAppointment("APT2", day.plusMinutes(70), 30, "Moved earlier");
    List<Appointment> appointments = service.getAppointmentsForDay(day.toLocalDate());
    assertEquals("APT1", appointments.get(0).getAppointmentId());
    assertEquals("APT2", appointments.get(1).getAppointmentId());

    assertThrows(SchedulingConflictException.class,
        () -> service.rescheduleAppointment("APT2", day.plusMinutes(30), 45, "Conflict"));
  }
}
