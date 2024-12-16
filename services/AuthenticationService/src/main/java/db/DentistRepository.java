package main.java.db;

import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DentistRepository extends MongoRepository<DentistSchema, String> {
    DentistSchema findByEmail(String email);
    int countByIsLoggedInTrue();
} 
    

