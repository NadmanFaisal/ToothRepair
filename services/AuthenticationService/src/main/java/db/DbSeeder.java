package main.java.db;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbSeeder implements CommandLineRunner {
    private final PatientRepository patientRepository;

    public DbSeeder(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Add a test patient
        PatientSchema patient = new PatientSchema();
        patient.setName("John Doe");
        patient.setEmail("bomboclat@gmail.com");
        patient.setPassword("tahavaibhav");
        patientRepository.save(patient);

        System.out.println("Sample patient inserted: " + patient);
    }
}
