package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import main.java.db.AppointmentRepository;
import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentSchema testAppointment;

    @BeforeEach
    void setup(){
        appointmentService = new AppointmentService(appointmentRepository);
        testAppointment = new AppointmentSchema();

        testAppointment.setStatus("available");
        testAppointment.setDentist("6edcvhy12345678jhgfvbyte34567");
        LocalDate date = LocalDate.now();
        
        testAppointment.setDate(date);
        testAppointment.setPatient("67fea12345678dd345");
        LocalTime startTime = LocalTime.of(9, 30, 0);
        LocalTime endTime = LocalTime.now();
        testAppointment.setStartTime(startTime);
        testAppointment.setEndTime(endTime);
        testAppointment.setClinic("6gj123456dfghj456");

    }

    @Test
    @DisplayName("Create and add a new appointment into the database")
    void addAppointmentTest(){

        when(appointmentRepository.save(testAppointment)).thenReturn(testAppointment);

        assertEquals(testAppointment, appointmentService.createAppointment(testAppointment));
        
        verify(appointmentRepository, times(1)).save(testAppointment);


    }

    @Test
    @DisplayName("Get all appointments in the database")
    void getAllAppointmentsTest(){
        AppointmentSchema secondAppointment = new AppointmentSchema();
        secondAppointment.setStatus("booked");
        secondAppointment.setDentist("6edcvhy12345678jhg");
        LocalDate date = LocalDate.now();
        secondAppointment.setDate(date);
        secondAppointment.setPatient("67fea12345678dytr456");
        LocalTime startTime = LocalTime.of(10, 30, 0);
        LocalTime endTime = LocalTime.now();
        secondAppointment.setStartTime(startTime);
        secondAppointment.setEndTime(endTime);
        secondAppointment.setClinic("6gj123456dfgh3456y");

        ArrayList<AppointmentSchema> appointments = new ArrayList<>();
        appointments.add(testAppointment);
        appointments.add(secondAppointment);

        when(appointmentRepository.findAll()).thenReturn(appointments);



        assertEquals(appointments, appointmentService.getAllAppointments(), "The two list of appointments should match");
        
        verify(appointmentRepository, times(1)).findAll();


    }

    @Test
    @DisplayName("Book an appointment in the db")
    void bookAppointmentTest(){

        when(appointmentRepository.findById(testAppointment.getId())).thenReturn(Optional.of(testAppointment));
        AppointmentSchema result = testAppointment;
        result.setStatus("booked");

        assertEquals(Optional.of(result), appointmentService.bookAppointment(testAppointment), "The result of this test object should match the booked appointment object");
        
        verify(appointmentRepository, times(1)).findById(testAppointment.getId());


    }








}
