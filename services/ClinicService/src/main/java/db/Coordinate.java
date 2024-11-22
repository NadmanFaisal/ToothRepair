package main.java.db;

public class Coordinate {
    private double latitude;
    private double longitude;


    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    //Setters
    public double setLatitude() {
        return latitude;
    }

    public double setLongitude() {
        return longitude;
    }
    
}
