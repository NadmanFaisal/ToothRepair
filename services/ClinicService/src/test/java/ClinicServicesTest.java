package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

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

    }

    @Test
    @DisplayName("Test to create a clinic")
    void createClinic(){
        
        when(clinicRepository.save(testClinic)).thenReturn(testClinic);

        assertEquals(testClinic, clinicService.createClinic(testClinic), "The two clinics should match");
        verify(clinicRepository,times(1)).save(testClinic);

    }

    @Test
    @DisplayName("Test getting all clinics in the database")
    void getAllClinicsTest(){
        ClinicSchema secondClinic = new ClinicSchema();
        testClinic.setAddress("Angered Village, Nowhere");
        Coordinate clinicCoordinate = new Coordinate(12.33456789067, 3.4234567889);
        secondClinic.setCoordinates(clinicCoordinate);
        secondClinic.setName("Taha's tooh repair clinic");
        ContactInfo clinicContactInfo = new ContactInfo("1234567890", "Taha.jasser@gmail.com");
        secondClinic.setContactInfo(clinicContactInfo);
        ArrayList<String> dentistIDs = new ArrayList<>();
        dentistIDs.add("6789farergvcdfgf3456789");
        secondClinic.setOpenHours("6:00 AM : 12:00PM");
        secondClinic.setDentists(dentistIDs);
        ArrayList<ClinicSchema> clinics = new ArrayList<>();
        clinics.add(secondClinic);
        clinics.add(testClinic);

        when(clinicRepository.findAll()).thenReturn(clinics);

        assertEquals(clinics, clinicService.getAllClinics(), "The two clinic lists should match");
        verify(clinicRepository, times(1)).findAll();

    }


    @Test
    @DisplayName("Get a clinic by its ID")
    void getClinicByIdTest(){

        testClinic.setId("6fa246fdbh23456");

        when(clinicRepository.findById(testClinic.getId())).thenReturn(Optional.of(testClinic));

        assertEquals(clinicService.getClinic(testClinic.getId()), Optional.of(testClinic), "The two clinics should be equal in the test");

        verify(clinicRepository, times(1)).findById(testClinic.getId());

    }

    @Test
    @DisplayName("Add a dentist to a clinic")
    void addDentistToClinicTest(){
       ArrayList<String> dentists = (ArrayList<String>) testClinic.getDentists();
        String dentistID = "6fa234567890asd34567";
        dentists.add(dentistID);
        
        when(clinicRepository.findById(testClinic.getId())).thenReturn(Optional.of(testClinic));

        ClinicSchema result = testClinic;

        result.setDentists(dentists);

        assertEquals(Optional.of(result), clinicService.addDentist(testClinic.getId(), dentistID ), "Both clinics should be equals and should have a list of 2 clinics");
        verify(clinicRepository, times(1)).findById(testClinic.getId());

    }



}