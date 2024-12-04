package main.java.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.db.LogRepository;
import main.java.db.LogSchema;

@Service
public class LogService{

    private LogRepository logRepository;
    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<LogSchema> getAllLogs() {
        return logRepository.findAll();
        
    }

    public LogSchema createLog(LogSchema log) {
        return logRepository.save(log);
    }


    public Optional<LogSchema> getLog(LogSchema log){
        return logRepository.findById(log.getId());
    }

    
    


}

