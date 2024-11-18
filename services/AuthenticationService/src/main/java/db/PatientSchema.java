package main.java.db;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "patients") 
public class PatientSchema {
    @Id
    private String id;
    private String name;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        String patientRepresentation = "";
        patientRepresentation = "{"+ "id: "+ this.id + ", \n" + "name: "+ this.name+ "}";
        return patientRepresentation;

    }

}
