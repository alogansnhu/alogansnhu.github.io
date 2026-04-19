package edu.snhu.cs499.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import edu.snhu.cs499.task.Task;

public class SchedulerFacadeTest {

  @Test
  void facade_coordinatesServicesAndSharedAuditTrail() {
    SchedulerFacade facade = new SchedulerFacade();

    facade.createContact("C1", "Drew", "Logan", "1234567890", "123 Main Street");
    facade.createTask("T1", "Write", "Write milestone", Task.Priority.HIGH, LocalDateTime.now().plusDays(2));
    facade.scheduleAppointment("A1", LocalDateTime.now().plusDays(3).withHour(10).withMinute(0), 60, "Review");

    assertEquals(3, facade.getAuditTrail().size());
    assertEquals(1, facade.getContactService().findAll().size());
    assertEquals(1, facade.getTaskService().findAll().size());
    assertEquals(1, facade.getAppointmentService().findAll().size());
  }
}
