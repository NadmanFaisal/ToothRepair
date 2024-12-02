package main.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import main.java.db.ClinicSchema;
import main.java.service.ClinicService;

import java.util.List;

@RestController
@RequestMapping("/clinics")
public class ClinicController {
    
    @Autowired
    private ClinicService clinicService;

    @GetMapping
    public List<ClinicSchema> getAllClinics() {
        return clinicService.getAllClinics();
    }

    @PostMapping
    public ClinicSchema createclinic(@RequestBody ClinicSchema clinic) {
        return clinicService.createClinic(clinic);
    }
}