package main.java.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

import java.util.List;

public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentSchema> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PostMapping
    public AppointmentSchema createAppointment(@RequestBody AppointmentSchema appointment) {
        return appointmentService.createAppointment(appointment);
    }
    
}
