package main.java.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "dentists")
public class DentistSchema { 
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String password;
    private String clinic;
    @JsonProperty("isLoggedIn")
    private boolean isLoggedIn;


    public boolean getIsLoggedIn(){
        return this.isLoggedIn;
    }

    public void setIsLoggedIn(boolean newLoggedInStatus){
        this.isLoggedIn = newLoggedInStatus;
    }
    
    public String getClinic() {
        return clinic;
    }
    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

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
        return inputPassword.equals(this.password);
    }

    @Override
    public String toString(){
        return "{"+ "id: "+ this.id + ", \n" + "name: "+ this.name+ ", \n" + "email: "+ this.email+ ", \n" + "clinic: "+ this.clinic +"}";
    }

}
