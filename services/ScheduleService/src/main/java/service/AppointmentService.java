package main.java.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.AppointmentRepository;
import main.java.db.AppointmentSchema;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
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
    public Optional<AppointmentSchema> makeAppointmentAvailable(AppointmentSchema appointmentInfo) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentInfo.getId());
    
        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();

            appointment.setDentist(appointmentInfo.getDentist());

            appointment.setStatus("available");

            
            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }

    }

    public Optional<AppointmentSchema> patientCancel(AppointmentSchema appointmentInfo) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentInfo.getId());
    
        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();

            appointment.setPatient(null);
            appointment.setStatus("available");

            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<AppointmentSchema> dentistCancel(AppointmentSchema appointmentInfo) {
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentInfo.getId());
    
        if (optionalAppointment.isPresent()) {
            AppointmentSchema appointment = optionalAppointment.get();
            
            appointment.setDentist(null);
            appointment.setPatient(null);
            appointment.setStatus("unavailable");

            appointmentRepository.save(appointment);

            return Optional.of(appointment);
        } else {
            return Optional.empty();
        }
    }

    public AppointmentSchema getApppoinment(String appointmentId){
        AppointmentSchema appointment = null;
        Optional<AppointmentSchema> optionalAppointment = appointmentRepository.findById(appointmentId);
        if(optionalAppointment.isPresent()){
            appointment = optionalAppointment.get();
        }
        System.out.println("THIS IS WHAT IS RETURNED FROM GET APPOINTMENT: " + appointment.toString());
        return appointment;
    }

    public List<AppointmentSchema> getAppointmentsByClinic(String clinicId) {
        return appointmentRepository.findByClinic(clinicId);
    }

    public List<AppointmentSchema> getAppointmentsByPatient(AppointmentSchema appointmentInfo) {
        return appointmentRepository.findByPatient(appointmentInfo.getPatient());
    }

    public List<AppointmentSchema> getAppointmentsByDentist(AppointmentSchema appointmentInfo) {
        return appointmentRepository.findByDentist(appointmentInfo.getDentist());
    }

    public int getTotalnumberOfAvailableAppointments(){
        return appointmentRepository.countByStatus("available");

    }

    
    
}
