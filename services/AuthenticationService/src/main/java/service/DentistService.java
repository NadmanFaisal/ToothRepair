package main.java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.DentistRepository;
import main.java.db.DentistSchema;


@Service
public class DentistService {

    private DentistRepository dentistRepository;
    @Autowired
    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    public List<DentistSchema> getAllDentists() {
        return dentistRepository.findAll();
        
    }

    public DentistSchema createDentist(DentistSchema dentist) {
        return dentistRepository.save(dentist);
    }
    public Boolean checkDuplicateDentist(DentistSchema dentist ) {
        
        if(dentistRepository.findByEmail(dentist.getEmail()) != null){
            return true;
        }
        return false;
    }

}
