package main.java.service;
import java.util.List;

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
    
}
