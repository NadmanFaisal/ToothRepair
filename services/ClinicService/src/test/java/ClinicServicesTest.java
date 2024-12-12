package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import main.java.db.ClinicRepository;
import main.java.db.ClinicSchema;
import main.java.db.ContactInfo;
import main.java.db.Coordinate;
import main.java.service.ClinicService;

@ExtendWith(MockitoExtension.class)
public class ClinicServicesTest {

    @Mock
    private ClinicRepository clinicRepository;
    @Mock
    private MongoTemplate mongoTemplate; 
    @InjectMocks
    private ClinicService clinicService;

    private ClinicSchema testClinic;


    @BeforeEach
    void setup(){
        clinicService = new ClinicService(clinicRepository, mongoTemplate);
        testClinic = new ClinicSchema();
    }

    @Test
    @DisplayName("Test to create a clinic")
    void createClinic(){
        testClinic.setAddress("Nordstan, Gothenburg");
        Coordinate clinicCoordinate = new Coordinate(12.34567, 3.456789);
        testClinic.setCoordinates(clinicCoordinate);
        testClinic.setName("Vaibhav and Taha's tooh repair clinic");
        ContactInfo clinicContactInfo = new ContactInfo("461234567890", "Vaibhav@gmail.com");
        testClinic.setContactInfo(clinicContactInfo);
        ArrayList<String> dentistIDs = new ArrayList<>();
        dentistIDs.add("6edcvhy12345678jhgfvbyte34567");
        testClinic.setOpenHours("10:00 AM : 12:00PM");
        testClinic.setDentists(dentistIDs);
        
        when(clinicRepository.save(testClinic)).thenReturn(testClinic);

        assertEquals(testClinic, clinicService.createClinic(testClinic));
        verify(clinicRepository,times(1)).save(testClinic);

    }





}