package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import main.java.db.DentistRepository;
import main.java.db.DentistSchema;
import main.java.db.PatientRepository;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;

@ExtendWith(MockitoExtension.class)
public class ServicesTest {
    
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DentistRepository dentistRepository;
   
    @InjectMocks
    private PatientService patientService;
    @InjectMocks
    private DentistService dentistService;

    private PatientSchema testPatient;
    
    private DentistSchema testDentist;
    
    @BeforeEach
    void setup(){
        patientService = new PatientService(patientRepository);
        dentistService = new DentistService(dentistRepository);
        
        testPatient = new PatientSchema();
        testDentist = new DentistSchema();
    }
    
    
    /* 
    @Test
    @DisplayName("Try to search for a patient by email and return true if they exists, false if they dont")
    void checkDuplicatePatientTest(){
        testPatient.setEmail("tokyo@gmail.com");
        when(patientService.checkDuplicatePatient(testPatient)).thenReturn(true);
        assertEquals(true, patientService.checkDuplicatePatient(testPatient), "Duplicate email should exists and true should be returned");
    }

    @Test
    @DisplayName("Try to search for a dentist by email and return true if they exists, false if they dont")
    void checkDuplicateDentistTest(){
        testDentist.setEmail("vaibhavpuram05@gmail.com");
        when(dentistService.checkDuplicateDentist(testDentist)).thenReturn(true);
        assertEquals(true, dentistService.checkDuplicateDentist(testDentist), "Duplicate email should exists and true should be returned");
    }
    
    */

    @Test
    @DisplayName("Create a patient and successfully save it to the database to be stored")
    void createPatientTest(){
        PatientSchema newPatient = new PatientSchema();
        newPatient.setEmail("YouAreMySunshine@gmail.com");  
        newPatient.setName("Taha");
        newPatient.setPassword("1234567890");     
        
        when(patientRepository.save(newPatient)).thenReturn(newPatient);

        PatientSchema result = patientService.createPatient(newPatient);

        assertEquals(newPatient, result, "The saved patient should match the input patient.");
        verify(patientRepository).save(newPatient); 
    }

    @Test
    @DisplayName("Create a dentist and successfully save it to the database to be stored")
    void createDentistTest(){
        DentistSchema newDentist = new DentistSchema();
        newDentist.setEmail("Mohamed.Taha@gmail.com");
        newDentist.setName("Vaibhav Puram");
        newDentist.setPassword("12345678");
        newDentist.setClinic("Taha Jasser Teeth Repair");

        when(dentistRepository.save(newDentist)).thenReturn(newDentist);

        DentistSchema result = dentistService.createDentist(newDentist);

        assertEquals(newDentist, result, "The saved dentist should match the input dentist.");
        verify(dentistRepository).save(newDentist);
        
    }
    

    


}
