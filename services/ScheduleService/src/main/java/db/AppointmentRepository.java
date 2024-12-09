package main.java.db;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppointmentRepository extends MongoRepository<AppointmentSchema, String> {

List<AppointmentSchema> findByClinic(String clinic);

}
