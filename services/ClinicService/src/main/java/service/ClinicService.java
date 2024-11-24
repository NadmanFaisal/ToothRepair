package main.java.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.ClinicRepository;
import main.java.db.ClinicSchema;

@Service
public class ClinicService {

    private ClinicRepository ClinicRepository;
    @Autowired
    public ClinicService(ClinicRepository ClinicRepository) {
        this.ClinicRepository = ClinicRepository;
    }

    public List<ClinicSchema> getAllClinics() {
        return ClinicRepository.findAll();
    }

    public ClinicSchema createClinic(ClinicSchema clinic) {
        return ClinicRepository.save(clinic);
    }
    
}
