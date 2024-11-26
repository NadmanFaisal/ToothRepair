package main.java.db;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Schedule") 
public class AppointmentSchema {
    @Id
    private String id;
    
    private String status;
    
    // @DBRef
    // private PatientSchema patient;
    
    // @DBRef
    // private ClinicSchema clinic;
    
    // @DBRef
    // private DentistSchema dentist;
    
    // private LocalDate date;
    
    // private LocalTime startTime;
    
    // private LocalTime endTime;

    // Getter methods to return attribute values
    public String getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    // public PatientSchema getPatient() {
    //     return this.patient;
    // }

    // public ClinicSchema getClinic() {
    //     return this.clinic;
    // }

    // public DentistSchema getDentist() {
    //     return this.dentist;
    // }

    // public LocalDate getDate() {
    //     return this.date;
    // }

    // public LocalTime getStartTime() {
    //     return this.startTime;
    // }

    // public LocalTime getEndTime() {
    //     return this.endTime;
    // }

    // Setter method to set values for attributes
    public void setStatus(String status) {
        this.status = status;
    }

    // public void setPatient(PatientSchema patient) {
    //     this.patient = patient;
    // }

    // public void setClinic(ClinicSchema clinic) {
    //     this.clinic = clinic;
    // }

    // public void setDentist(DentistSchema dentist) {
    //     this.dentist = dentist;
    // }

    // public void setDate(LocalDate date) {
    //     this.date = date;
    // }

    // public void setStartTime(LocalTime startTime) {
    //     this.startTime = startTime;
    // }

    // public void setEndTime(LocalTime endTime) {
    //     this.endTime = endTime;
    // }

    // Stringyfies the appointment data
    public String toString() {
        String appointmentRepresentation = "";
        appointmentRepresentation = "{ id: " + this.id + ", status: " + this.status + " }";

        return appointmentRepresentation;
    }
}
