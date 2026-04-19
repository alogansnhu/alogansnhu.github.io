package edu.snhu.cs499.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import edu.snhu.cs499.appointment.Appointment;
import edu.snhu.cs499.appointment.AppointmentService;
import edu.snhu.cs499.audit.AuditEvent;
import edu.snhu.cs499.audit.InMemoryAuditLogger;
import edu.snhu.cs499.common.InMemoryRepository;
import edu.snhu.cs499.contact.Contact;
import edu.snhu.cs499.contact.ContactService;
import edu.snhu.cs499.task.Task;
import edu.snhu.cs499.task.TaskService;

public class SchedulerFacade {
  private final InMemoryAuditLogger auditLogger;
  private final ContactService contactService;
  private final TaskService taskService;
  private final AppointmentService appointmentService;

  public SchedulerFacade() {
    this.auditLogger = new InMemoryAuditLogger();
    this.contactService = new ContactService(new InMemoryRepository<>(), auditLogger);
    this.taskService = new TaskService(new InMemoryRepository<>(), auditLogger);
    this.appointmentService = new AppointmentService(new InMemoryRepository<>(), auditLogger);
  }

  public Contact createContact(String contactId, String firstName, String lastName, String phone, String address) {
    return contactService.createContact(contactId, firstName, lastName, phone, address);
  }

  public Contact updateContact(String contactId, String firstName, String lastName, String phone, String address) {
    return contactService.updateContact(contactId, firstName, lastName, phone, address);
  }

  public boolean deleteContact(String contactId) {
    return contactService.deleteById(contactId);
  }

  public Task createTask(String taskId, String name, String description, Task.Priority priority,
      LocalDateTime dueDate) {
    return taskService.createTask(taskId, name, description, priority, dueDate);
  }

  public Task updateTask(String taskId, String name, String description, Task.Priority priority,
      LocalDateTime dueDate) {
    return taskService.updateTask(taskId, name, description, priority, dueDate);
  }

  public boolean deleteTask(String taskId) {
    return taskService.deleteById(taskId);
  }

  public List<Task> getTasksByPriority() {
    return taskService.getTasksByPriority();
  }

  public Appointment scheduleAppointment(String appointmentId, LocalDateTime startTime, int durationMinutes,
      String description) {
    return appointmentService.scheduleAppointment(appointmentId, startTime, durationMinutes, description);
  }

  public Appointment rescheduleAppointment(String appointmentId, LocalDateTime startTime, int durationMinutes,
      String description) {
    return appointmentService.rescheduleAppointment(appointmentId, startTime, durationMinutes, description);
  }

  public boolean deleteAppointment(String appointmentId) {
    return appointmentService.deleteById(appointmentId);
  }

  public List<Appointment> getAppointmentsForDay(LocalDate date) {
    return appointmentService.getAppointmentsForDay(date);
  }

  public List<AuditEvent> getAuditTrail() {
    return auditLogger.getEvents();
  }

  public ContactService getContactService() {
    return contactService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public AppointmentService getAppointmentService() {
    return appointmentService;
  }
}
