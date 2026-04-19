package edu.snhu.cs320.appointments;
import java.util.Date;

public class Appointment {
    private final String appointmentId;         // not updatable
    private Date appointmentDate;               // must not be in the past
    private String appointmentDescription;      // <= 50 chars

    public Appointment(String appointmentId, Date appointmentDate, String appointmentDescription) {
        validateId(appointmentId);
        validateDate(appointmentDate);
        validateDescription(appointmentDescription);

        this.appointmentId = appointmentId;
        // defensive copy
        this.appointmentDate = new Date(appointmentDate.getTime());
        this.appointmentDescription = appointmentDescription;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public Date getAppointmentDate() {
        // defensive copy
        return new Date(appointmentDate.getTime());
    }

    public String getAppointmentDescription() {
        return appointmentDescription;
    }

    /**
     * Optional mutator for appointmentDate (ID is intentionally immutable).
     * Enforces "not in the past" and non-null on every update.
     */
    public void setAppointmentDate(Date newDate) {
        validateDate(newDate);
        this.appointmentDate = new Date(newDate.getTime());
    }

    /**
     * Optional mutator for description to allow later edits.
     */
    public void setAppointmentDescription(String newDescription) {
        validateDescription(newDescription);
        this.appointmentDescription = newDescription;
    }

    // Validation helpers

    private static void validateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Appointment ID must not be null.");
        }
        if (id.length() > 10) {
            throw new IllegalArgumentException("Appointment ID must be at most 10 characters.");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Appointment ID must not be empty.");
        }
    }

    private static void validateDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Appointment date must not be null.");
        }
        // cannot be in the past, relative to now
        if (date.before(new Date())) {
            throw new IllegalArgumentException("Appointment date must not be in the past.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Appointment description must not be null.");
        }
        if (description.length() > 50) {
            throw new IllegalArgumentException("Appointment description must be at most 50 characters.");
        }
    }
}