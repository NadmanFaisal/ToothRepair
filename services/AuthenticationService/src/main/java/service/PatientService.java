package main.java.service;

import static org.mockito.ArgumentMatchers.booleanThat;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.PatientRepository;
import main.java.db.PatientSchema;

@Service
public class PatientService{

    private final PatientRepository patientRepository;
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

    public Boolean checkDuplicatePatient(PatientSchema patient ) {
        
        return patientRepository.findByEmail(patient.getEmail()) != null;
    }

    public PatientSchema getPatient(PatientSchema patient){
        return patientRepository.findByEmail(patient.getEmail());
    }

    public PatientSchema getPatientByID(String id){
        Optional<PatientSchema> optionalPatient = patientRepository.findById(id);
        PatientSchema patient;
        if(optionalPatient.isPresent()){
            patient = optionalPatient.get();
        }else{
            System.out.println("Patient Cannot be found in the DB");
            patient = null;
        }
        return patient;
    }

    public int getActivePatients(){
        return patientRepository.countByIsLoggedInTrue();
    }

    public void setIsLoggedIn(PatientSchema patient, boolean loggedInStatus){
        
            patient.setIsLoggedIn(loggedInStatus);
            patientRepository.save(patient);

    }
        
}

