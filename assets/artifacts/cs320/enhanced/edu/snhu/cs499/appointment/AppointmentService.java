package edu.snhu.cs499.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.snhu.cs499.audit.AuditAction;
import edu.snhu.cs499.audit.AuditLogger;
import edu.snhu.cs499.audit.InMemoryAuditLogger;
import edu.snhu.cs499.common.AbstractCrudService;
import edu.snhu.cs499.common.InMemoryRepository;
import edu.snhu.cs499.common.Repository;
import edu.snhu.cs499.common.ValidationUtils;
import edu.snhu.cs499.exceptions.SchedulingConflictException;

public class AppointmentService extends AbstractCrudService<Appointment> {
  private static final Comparator<Appointment> APPOINTMENT_ORDER = Comparator.comparing(Appointment::getStartTime)
      .thenComparing(Appointment::getAppointmentId);

  private final NavigableMap<LocalDate, List<Appointment>> appointmentsByDay = new TreeMap<>();

  public AppointmentService() {
    this(new InMemoryRepository<>(), new InMemoryAuditLogger());
  }

  public AppointmentService(Repository<Appointment, String> repository, AuditLogger auditLogger) {
    super("Appointment", repository, auditLogger);
  }

  public Appointment scheduleAppointment(String appointmentId, LocalDateTime startTime, int durationMinutes,
      String description) {
    return create(new Appointment(appointmentId, startTime, durationMinutes, description));
  }

  public Appointment rescheduleAppointment(String appointmentId, LocalDateTime newStartTime, int newDurationMinutes,
      String newDescription) {
    Appointment existing = requireById(appointmentId);
    LocalDateTime oldStart = existing.getStartTime();
    int oldDuration = existing.getDurationMinutes();
    String oldDescription = existing.getAppointmentDescription();

    removeFromDayIndex(existing);
    try {
      existing.setStartTime(newStartTime);
      existing.setDurationMinutes(newDurationMinutes);
      existing.setAppointmentDescription(newDescription);
      insertIntoDayIndex(existing);
      repository.save(existing);
      record(AuditAction.UPDATE, existing.getId(), "Rescheduled appointment.");
      return existing;
    } catch (RuntimeException ex) {
      existing.setStartTime(oldStart);
      existing.setDurationMinutes(oldDuration);
      existing.setAppointmentDescription(oldDescription);
      insertIntoDayIndex(existing);
      throw ex;
    }
  }

  public List<Appointment> getAppointmentsForDay(LocalDate date) {
    ValidationUtils.requireNotNull(date, "Date");
    return Collections.unmodifiableList(new ArrayList<>(appointmentsByDay.getOrDefault(date, Collections.emptyList())));
  }

  public List<Appointment> getAppointmentsInRange(LocalDate startDate, LocalDate endDate) {
    ValidationUtils.requireNotNull(startDate, "Start date");
    ValidationUtils.requireNotNull(endDate, "End date");
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must not be before start date.");
    }
    return appointmentsByDay.subMap(startDate, true, endDate, true)
        .values()
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
  }

  @Override
  protected void beforeCreate(Appointment entity) {
    validateAndInsert(entity);
  }

  @Override
  protected void afterDelete(Appointment entity) {
    removeFromDayIndex(entity);
  }

  private void validateAndInsert(Appointment appointment) {
    List<Appointment> dayBucket = appointmentsByDay.computeIfAbsent(appointment.getAppointmentDate(),
        key -> new ArrayList<>());
    int insertIndex = findInsertIndex(dayBucket, appointment);
    validateNoConflict(dayBucket, appointment, insertIndex);
    dayBucket.add(insertIndex, appointment);
  }

  private void insertIntoDayIndex(Appointment appointment) {
    List<Appointment> dayBucket = appointmentsByDay.computeIfAbsent(appointment.getAppointmentDate(),
        key -> new ArrayList<>());
    int insertIndex = findInsertIndex(dayBucket, appointment);
    dayBucket.add(insertIndex, appointment);
  }

  private int findInsertIndex(List<Appointment> dayBucket, Appointment appointment) {
    int index = Collections.binarySearch(dayBucket, appointment, APPOINTMENT_ORDER);
    return index >= 0 ? index : -index - 1;
  }

  private void validateNoConflict(List<Appointment> dayBucket, Appointment candidate, int insertIndex) {
    if (insertIndex > 0) {
      Appointment previous = dayBucket.get(insertIndex - 1);
      if (previous.overlaps(candidate)) {
        throw new SchedulingConflictException(
            "Appointment conflicts with previous appointment: " + previous.getAppointmentId());
      }
    }

    if (insertIndex < dayBucket.size()) {
      Appointment next = dayBucket.get(insertIndex);
      if (next.overlaps(candidate)) {
        throw new SchedulingConflictException(
            "Appointment conflicts with next appointment: " + next.getAppointmentId());
      }
    }
  }

  private void removeFromDayIndex(Appointment appointment) {
    List<Appointment> dayBucket = appointmentsByDay.get(appointment.getAppointmentDate());
    if (dayBucket != null) {
      dayBucket.remove(appointment);
      if (dayBucket.isEmpty()) {
        appointmentsByDay.remove(appointment.getAppointmentDate());
      }
    }
  }
}
