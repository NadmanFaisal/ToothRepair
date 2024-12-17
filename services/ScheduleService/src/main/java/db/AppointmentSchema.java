package main.java.db;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

@Document(collection = "Schedule") 
public class AppointmentSchema {
    @Id
    private String id;
    
    private String status;
    
    private String patient;
    
    private String clinic;
    
    private String dentist;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    // Getter methods to return attribute values
    public String getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    public String getPatient() {
        return this.patient;
    }

    public String getClinic() {
        return this.clinic;
    }

    public String getDentist() {
        return this.dentist;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    // Setter method to set values for attributes
    public void setStatus(String status) {
        this.status = status;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public void setDentist(String dentist) {
        this.dentist = dentist;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    // Stringyfies the appointment data
    @Override
    public String toString() {
        String appointmentRepresentation = "{ id: " + this.id + ", status: " + this.status + ", date: " + this.date + 
        ", startTime: " + this.startTime + ", endTime: " + this.endTime + ", patient: " + this.patient +
        ", dentist: " + this.dentist +  ", clinic: " + this.clinic +  " }";

        return appointmentRepresentation;
    }
}
