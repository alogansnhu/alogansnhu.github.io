package edu.snhu.cs499.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import edu.snhu.cs499.common.Identifiable;
import edu.snhu.cs499.common.ValidationUtils;

public class Appointment implements Identifiable<String> {
  private final String appointmentId;
  private LocalDateTime startTime;
  private int durationMinutes;
  private String appointmentDescription;

  public Appointment(String appointmentId, LocalDateTime startTime, int durationMinutes,
      String appointmentDescription) {
    this.appointmentId = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(appointmentId, "Appointment ID"),
        10,
        "Appointment ID");
    this.startTime = ValidationUtils.requireFutureOrPresent(startTime, "Appointment start time");
    this.durationMinutes = ValidationUtils.requirePositive(durationMinutes, "Appointment duration");
    this.appointmentDescription = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(appointmentDescription, "Appointment description"),
        50,
        "Appointment description");
  }

  @Override
  public String getId() {
    return appointmentId;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDate getAppointmentDate() {
    return startTime.toLocalDate();
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public String getAppointmentDescription() {
    return appointmentDescription;
  }

  public LocalDateTime getEndTime() {
    return startTime.plusMinutes(durationMinutes);
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = ValidationUtils.requireFutureOrPresent(startTime, "Appointment start time");
  }

  public void setDurationMinutes(int durationMinutes) {
    this.durationMinutes = ValidationUtils.requirePositive(durationMinutes, "Appointment duration");
  }

  public void setAppointmentDescription(String appointmentDescription) {
    this.appointmentDescription = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(appointmentDescription, "Appointment description"),
        50,
        "Appointment description");
  }

  public boolean overlaps(Appointment other) {
    Objects.requireNonNull(other, "other appointment must not be null");
    return startTime.isBefore(other.getEndTime()) && other.getStartTime().isBefore(getEndTime());
  }
}
