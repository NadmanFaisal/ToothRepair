package main.java.service;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.DentistRepository;
import main.java.db.DentistSchema;



@Service
public class DentistService {

    private final DentistRepository dentistRepository;
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
        
        return dentistRepository.findByEmail(dentist.getEmail()) != null;
    }

    public DentistSchema getDentist(DentistSchema dentist){
        return dentistRepository.findByEmail(dentist.getEmail());
    }

    public String getNameByID(String dentistID){
        Optional<DentistSchema> dentist =  dentistRepository.findById(dentistID);
        return dentist.get().getName();
        
    }

    public String getEmailById(String dentistId) {
        Optional<DentistSchema> dentist = dentistRepository.findById(dentistId);
        return dentist.get().getEmail();
    }

    public String getClinicIdByDentistId(String dentistId) {
        Optional<DentistSchema> dentist = dentistRepository.findById(dentistId);
        if (dentist.isPresent()) {
            return dentist.get().getClinic();
        } else {
            return "Dentist with id: " + dentistId + " not found";
        }
    }

    public DentistSchema getDentistByID(String id){
        Optional<DentistSchema> optionalDentist = dentistRepository.findById(id);
        DentistSchema dentist;
        if(optionalDentist.isPresent()){
            dentist = optionalDentist.get();
        }else{
            System.out.println("Patient Cannot be found in the DB");
            dentist = null;
        }
        return dentist;
    }

    public int getActiveDentists(){
       return dentistRepository.countByIsLoggedInTrue();

    }

    public void setIsLoggedIn(DentistSchema dentist, boolean loggedInStatus){

            dentist.setIsLoggedIn(loggedInStatus);
            dentistRepository.save(dentist);
    }


}
