package edu.snhu.cs499.demo;

import java.time.LocalDateTime;

import edu.snhu.cs499.facade.SchedulerFacade;
import edu.snhu.cs499.task.Task;

public class EnhancementSmokeTest {
  public static void main(String[] args) {
    SchedulerFacade facade = new SchedulerFacade();

    facade.createContact("C1", "Drew", "Logan", "1234567890", "123 Main Street");
    facade.createTask("T1", "Write", "Write milestone", Task.Priority.HIGH, LocalDateTime.now().plusDays(2));
    facade.scheduleAppointment("A1", LocalDateTime.now().plusDays(3).withHour(10).withMinute(0), 60, "Review");

    facade.updateTask("T1", "Write", "Write final milestone", Task.Priority.CRITICAL, LocalDateTime.now().plusDays(1));
    facade.rescheduleAppointment("A1", LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), 45, "Review moved");

    System.out.println("Contacts: " + facade.getContactService().findAll().size());
    System.out.println("Top task: " + facade.getTasksByPriority().get(0).getTaskId());
    System.out.println("Appointments on review day: "
        + facade.getAppointmentsForDay(LocalDateTime.now().plusDays(3).toLocalDate()).size());
    System.out.println("Audit events: " + facade.getAuditTrail().size());
  }
}
