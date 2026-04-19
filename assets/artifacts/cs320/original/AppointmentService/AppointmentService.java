package edu.snhu.cs320.appointments;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AppointmentService {
    private final Map<String, Appointment> appointments = new HashMap<>();

    /**
     * Adds the given appointment if its ID is unique.
     * @throws IllegalArgumentException if the appointment is null or the ID already exists.
     */
    public Appointment addAppointment(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment must not be null");
        String id = appointment.getAppointmentId();
        if (appointments.containsKey(id)) {
            throw new IllegalArgumentException("Appointment ID must be unique: " + id);
        }
        appointments.put(id, appointment);
        return appointment;
    }

    /**
     * Deletes the appointment with the given ID.
     * @return true if an appointment was removed; false if no appointment existed with that ID.
     * @throws NullPointerException if appointmentId is null.
     */
    public boolean deleteAppointment(String appointmentId) {
        Objects.requireNonNull(appointmentId, "appointmentId must not be null");
        return appointments.remove(appointmentId) != null;
    }

    /** Convenience accessor. */
    public Optional<Appointment> getAppointment(String appointmentId) {
        Objects.requireNonNull(appointmentId, "appointmentId must not be null");
        return Optional.ofNullable(appointments.get(appointmentId));
    }

    /** Convenience size accessor for assertions. */
    public int size() {
        return appointments.size();
    }
}