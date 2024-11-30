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

    public Optional<AppointmentSchema> bookAppointment(String id) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(id);

        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();

            appointment.setStatus("booked");

            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }

    }
    
}
