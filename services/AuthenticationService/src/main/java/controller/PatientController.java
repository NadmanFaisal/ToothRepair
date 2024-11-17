package main.java.controller;

import java.util.List;

import org.springframework.stereotype.Service;

import main.java.db.PatientSchema;

@Service
public class PatientController{

    private PatientControllerInterface patientRepository;

    public PatientController(PatientControllerInterface patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientSchema> getAllPatients() {
        return patientRepository.findAll();
    }

    public PatientSchema createPatient(PatientSchema patient) {
        return patientRepository.save(patient);
    }

}

