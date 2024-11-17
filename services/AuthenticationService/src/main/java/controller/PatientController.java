package main.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import main.java.db.PatientSchema;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {
    
    @Autowired
    private PatientService patientService;

    //@GetMapping
    public List<PatientSchema> getAllPatients() {
        return patientService.getAllPatients();
    }

    //@PostMapping
    public PatientSchema createpatient(@RequestBody PatientSchema patient) {
        return patientService.createPatient(patient);
    }
}