package main.java.db;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DentistRepository extends MongoRepository<DentistSchema, String> {
    static PatientSchema findByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByEmail'");
    }
} 
    

