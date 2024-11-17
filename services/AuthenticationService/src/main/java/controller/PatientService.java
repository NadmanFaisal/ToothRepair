package main.java.controller;

import java.util.List;

import org.springframework.stereotype.Service;

import main.java.db.PatientSchema;

@Service
public class PatientService{

    private PatientServiceInterface patientRepository;

    public PatientService(PatientServiceInterface patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientSchema> getAllPatients() {
        return patientRepository.findAll();
    }

    public PatientSchema createPatient(PatientSchema patient) {
        return patientRepository.save(patient);
    }



}

