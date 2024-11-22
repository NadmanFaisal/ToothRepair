package main.java.db;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClinicRepository extends MongoRepository<ClinicSchema, String> {

} 
