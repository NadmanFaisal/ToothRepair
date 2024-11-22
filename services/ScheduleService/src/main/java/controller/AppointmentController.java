package main.java.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import main.java.db.AppointmentSchema;
import main.java.service.BookingService;

import java.util.List;

public class AppointmentController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<AppointmentSchema> getAllAppointmentSchemas() {
        return bookingService.getAllAppointments();
    }

    @PostMapping
    public AppointmentSchema createAppointment(@RequestBody AppointmentSchema appointment) {
        return bookingService.createAppointment(appointment);
    }
    
}
