package main.java.db;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<LogSchema, String> {
    
} 
    

