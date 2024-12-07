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
        return inputPassword.equals(this.password);
    }

    @Override
    public String toString(){
        return "{"+ "id: "+ this.id + ", \n" + "name: "+ this.name+ ", \n" + "email: "+ this.email+"}";
    }

}
