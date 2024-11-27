package main.java.db;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DentistRepository extends MongoRepository<DentistSchema, String> {

} 
    

