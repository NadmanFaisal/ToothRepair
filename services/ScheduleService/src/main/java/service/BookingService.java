package main.java.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.AppointmentRepository;
import main.java.db.AppointmentSchema;

@Service
public class BookingService {

    private AppointmentRepository appointmentRepository;
    @Autowired
    public BookingService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<AppointmentSchema> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public AppointmentSchema createAppointment(AppointmentSchema appointment) {
        return appointmentRepository.save(appointment);
    }
    
}
