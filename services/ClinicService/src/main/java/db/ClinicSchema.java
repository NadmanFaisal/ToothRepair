package main.java.db;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "clinics") 
public class ClinicSchema {
    @Id
    private String id;

    private String name;
    
    private Coordinate coordinate;
    
    private String address;
    
    private String open_hours;
    
    private ContactInfo contact_info;
    
    private List<String> dentists;

    //Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Coordinate getCoordinates() {
        return coordinate;
    }
    
    public String getAdress() {
        return address;
    }
    
    public String getOpenHours() {
        return open_hours;
    }
    
    public ContactInfo getContactInfo() {
        return contact_info;
    }

    public List<String> getDentistIDs() {
        return dentists;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setOpenHours(String open_hours) {
        this.open_hours = open_hours;
    }

    public void setContactInfo(ContactInfo contact_info) {
        this.contact_info = contact_info;
    }

    public void setDentists(List<String> dentists) {
        this.dentists = dentists;
    }

}
