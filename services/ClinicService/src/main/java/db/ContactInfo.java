package main.java.db;

public class ContactInfo {
    private String number;
    private String email;

    public ContactInfo() {
        
    }

    public ContactInfo(String number, String email) {
        this.number = number;
        this.email = email;
    }

    //Getters
    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    //Setters
    public void setNumber(String number) {
        this.number = number;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
} 
    
