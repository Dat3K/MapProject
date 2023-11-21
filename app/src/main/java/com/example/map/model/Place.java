package com.example.map.model;


import androidx.annotation.NonNull;

public class Place {
    String name;
    String address;

    double latitude;

    double longitude;

    double distance;


    public Place(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = 0;
    }

    public Place() {
        this.name = "";
        this.address = "";
        this.latitude = 0;
        this.longitude = 0;
        this.distance = 0;
    }

    public Place(String name, String address, double distance){
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    @NonNull
    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                '}';
    }

}
