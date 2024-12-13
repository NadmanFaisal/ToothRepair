package main.java.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.AppointmentRepository;
import main.java.db.AppointmentSchema;

@Service
public class AppointmentService {

    private AppointmentRepository appointmentRepository;
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<AppointmentSchema> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public AppointmentSchema createAppointment(AppointmentSchema appointment) {
        return appointmentRepository.save(appointment);
    }

    public Optional<AppointmentSchema> bookAppointment(AppointmentSchema appointmentInfo) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentInfo.getId());

        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();

            appointment.setPatient(appointmentInfo.getPatient());

            appointment.setStatus("booked");

            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }

    }

    //might need to refactor
    public Optional<AppointmentSchema> changeAppointmentStatus(AppointmentSchema appointmentInfo) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentInfo.getId());
    
        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();

            appointment.setDentist(appointmentInfo.getDentist());

            if (appointment.getStatus().equals("unavailable")) {
                appointment.setStatus("available");
            } else if (appointment.getStatus().equals("available") || (appointment.getStatus().equals("booked"))) {
                appointment.setStatus("unavailable");
            }

            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }

    }

    public List<AppointmentSchema> getAppointmentsByClinic(AppointmentSchema appointmentInfo) {
        return appointmentRepository.findByClinic(appointmentInfo.getClinic());
    }

    public List<AppointmentSchema> getAppointmentsByPatient(AppointmentSchema appointmentInfo) {
        return appointmentRepository.findByPatient(appointmentInfo.getPatient());
    }
    
}
