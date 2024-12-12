package main.java.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "patients") 
public class PatientSchema {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }    
    public void setEmail(String newEmail){
        this.email = newEmail;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String newPassword){
        this.password = newPassword;
    }
    
    public boolean checkPassword(String inputPassword){
        if(inputPassword.equals(this.password)){
            return true;
        }
        return false;
    }

    public String toString(){
        String patientRepresentation = "";
        patientRepresentation = "{"+ "id: "+ this.id + ", \n" + "name: "+ this.name+ ", \n" + "email: "+ this.email+"}";
        return patientRepresentation;

    }
    public boolean equals (PatientSchema otherPatient){
        boolean isEqual = false;
        if(otherPatient == this){
            isEqual = true;
        }else if (otherPatient == null){
            isEqual = false;
        }else if (otherPatient instanceof PatientSchema){
            boolean sameName = this.getName().equals(otherPatient.getName());
            boolean samePassword = this.getPassword().equals(otherPatient.getPassword());
            boolean sameEmail = this.getEmail().equals(otherPatient.getEmail());
            isEqual = sameEmail && sameName && samePassword;
        }else{
            return isEqual;
        }

        return isEqual;

    }
}
