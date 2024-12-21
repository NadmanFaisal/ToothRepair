package main.java.db;

import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatientRepository extends MongoRepository<PatientSchema, String> {
    PatientSchema findByEmail(String email);
    int countByIsLoggedInTrue();
} 
    

