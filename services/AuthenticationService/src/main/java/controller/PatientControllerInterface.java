package main.java.controller;

import org.springframework.data.mongodb.repository.MongoRepository;

import main.java.db.PatientSchema;

public interface PatientControllerInterface extends MongoRepository<PatientSchema, String> {

} 
    

