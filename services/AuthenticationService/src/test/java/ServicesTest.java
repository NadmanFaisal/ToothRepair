package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
    
    
    @Test
    @DisplayName("Try to search for a patient by email and return true if they exists, false if they dont")
    void checkDuplicatePatientTest(){
        testPatient.setEmail("tokyo@gmail.com");
        when(patientRepository.findByEmail("tokyo@gmail.com")).thenReturn(testPatient);
        assertEquals(true, patientService.checkDuplicatePatient(testPatient), "Duplicate email should exists and true should be returned");
        verify(patientRepository, times(1)).findByEmail("tokyo@gmail.com");
    }

    
    @Test
    @DisplayName("Try to search for a dentist by email and return true if they exists, false if they dont")
    void checkDuplicateDentistTest(){
        testDentist.setEmail("vaibhavpuram05@gmail.com");
        when(dentistRepository.findByEmail("vaibhavpuram05@gmail.com")).thenReturn(testDentist);
        assertEquals(true, dentistService.checkDuplicateDentist(testDentist), "Duplicate email should exists and true should be returned");
        verify(dentistRepository, times(1)).findByEmail("vaibhavpuram05@gmail.com");
    }
    

    @Test
    @DisplayName("Create a patient and successfully save it to the database to be stored")
    void createPatientTest(){
        testPatient.setEmail("YouAreMySunshine@gmail.com");  
        testPatient.setName("Taha");
        testPatient.setPassword("1234567890");     
        
        when(patientRepository.save(testPatient)).thenReturn(testPatient);

        PatientSchema result = patientService.createPatient(testPatient);

        assertEquals(testPatient, result, "The saved patient should match the input patient.");
        verify(patientRepository).save(testPatient); 
    }

    @Test
    @DisplayName("Create a dentist and successfully save it to the database to be stored")
    void createDentistTest(){
        testDentist.setEmail("Mohamed.Taha@gmail.com");
        testDentist.setName("Vaibhav Puram");
        testDentist.setPassword("12345678");
        testDentist.setClinic("Taha Jasser Teeth Repair");

        when(dentistRepository.save(testDentist)).thenReturn(testDentist);

        DentistSchema result = dentistService.createDentist(testDentist);

        assertEquals(testDentist, result, "The saved dentist should match the input dentist.");
        verify(dentistRepository).save(testDentist);
        
    }
    

    


}
