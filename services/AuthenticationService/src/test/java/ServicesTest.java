package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import main.java.db.DentistSchema;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;

@ExtendWith(MockitoExtension.class)
public class ServicesTest {
    @Mock
    private PatientService patientService;
    @Mock
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
    void checkDuplicatePatientTest(){
        testPatient.setEmail("tokyo@gmail.com");
        when(patientService.checkDuplicatePatient(any())).thenReturn(true);
        assertEquals(true, patientService.checkDuplicatePatient(testPatient), "Duplicate email should exists and true should be returned");
    }

    @Test
    @DisplayName("Try to search for a dentist by email and return true if they exists, false if they dont")
    void checkDuplicateDentistTest(){
        testDentist.setEmail("vaibhavpuram05@gmail.com");
        when(dentistService.checkDuplicateDentist(any())).thenReturn(true);
        assertEquals(true, dentistService.checkDuplicateDentist(testDentist), "Duplicate email should exists and true should be returned");
    }

    

    


}
