package main.java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.PatientRepository;
import main.java.db.PatientSchema;

@Service
public class PatientService{

    private PatientRepository patientRepository;
    @Autowired
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

