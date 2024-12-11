package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import main.java.db.DentistSchema;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;

@SpringBootTest
public class ServicesTest {
    @Mock
    private PatientService patientService;
    @Mock
    private DentistService dentistService;
    @InjectMocks
    private PatientSchema testPatient;
    @InjectMocks
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
