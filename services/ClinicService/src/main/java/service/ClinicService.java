package main.java.service;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import main.java.db.ClinicRepository;
import main.java.db.ClinicSchema;

@Service
public class ClinicService {

    private ClinicRepository ClinicRepository;
    private MongoTemplate mongoTemplate;
    @Autowired
    public ClinicService(ClinicRepository ClinicRepository, MongoTemplate mongoTemplate) {
        this.ClinicRepository = ClinicRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<ClinicSchema> getAllClinics() {
        return ClinicRepository.findAll();
    }

    public ClinicSchema createClinic(ClinicSchema clinic) {
        return ClinicRepository.save(clinic);
    }

    public void addDentist(String clinicId, String dentistId) {
        Query query = new Query(Criteria.where("_id").is(clinicId));
        Update update = new Update().push("dentists", dentistId);

        mongoTemplate.updateFirst(query, update, ClinicSchema.class);
    }
    
}
