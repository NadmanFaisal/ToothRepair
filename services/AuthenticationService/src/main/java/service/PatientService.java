package main.java.service;

import java.util.List;

import org.springframework.stereotype.Service;

import main.java.db.PatientRepository;
import main.java.db.PatientSchema;

@Service
public class PatientService{

    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientSchema> getAllPatients() {
        return patientRepository.findAll();
    }

    public PatientSchema createPatient(PatientSchema patient) {
        return patientRepository.save(patient);
    }



}

