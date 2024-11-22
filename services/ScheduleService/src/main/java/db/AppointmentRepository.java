package main.java.db;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppointmentRepository extends MongoRepository<AppointmentSchema, String> {

}
