package main.java.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import main.java.db.ClinicRepository;
import main.java.db.ClinicSchema;

@Service
public class ClinicService {

    private ClinicRepository ClinicRepository;

    @Autowired
    public ClinicService(ClinicRepository ClinicRepository, MongoTemplate mongoTemplate) {
        this.ClinicRepository = ClinicRepository;
    }

    public List<ClinicSchema> getAllClinics() {
        return ClinicRepository.findAll();
    }

    public Optional<ClinicSchema> getClinic(String clinicId) {
        return ClinicRepository.findById(clinicId);
    }

    public ClinicSchema createClinic(ClinicSchema clinic) {
        return ClinicRepository.save(clinic);
    }

    public Optional<ClinicSchema> addDentist(String clinicId, String dentistId) {
        Optional<ClinicSchema> optionalClinic = ClinicRepository.findById(clinicId);

        if (optionalClinic.isPresent()) {
            ClinicSchema clinic = optionalClinic.get();

            clinic.getDentists().add(dentistId);

            ClinicRepository.save(clinic);

            return Optional.of(clinic);
        } else {
            return Optional.empty();
        }
    }
    
}
