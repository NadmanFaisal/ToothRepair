package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import main.java.db.DentistRepository;
import main.java.db.DentistSchema;
import main.java.db.PatientRepository;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;


public class ServicesTest {
    private PatientService patientService;
    private DentistService dentistService;
    private PatientSchema testPatient;
    private DentistSchema testDentist;
    
    @BeforeEach
    void setup(){
        testPatient = new PatientSchema();
        testDentist = new DentistSchema();
    }
    
    

    @Test
    @DisplayName("Try to search for a patient by email and return true if they exists, false if they dont")
    void checkDuplicatePatient(){
        testPatient.setEmail("Vaibhavpuram05@gmail.com");
        assertEquals(true, patientService.checkDuplicatePatient(testPatient), "Duplicate email should exists and true should be returned");
    }

    @Test
    @DisplayName("Try to search for a dentist by email and return true if they exists, false if they dont")
    void checkDuplicateDentist(){
        testDentist.setEmail("Vaibhavpuram05@gmail.com");
        assertEquals(true, dentistService.checkDuplicateDentist(testDentist), "Duplicate email should exists and true should be returned");
    }

    

    


}
