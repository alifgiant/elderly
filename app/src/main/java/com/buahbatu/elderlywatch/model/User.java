package com.buahbatu.elderlywatch.model;

/**
 * Created by maakbar on 4/11/17.
 */

public class User {
    public String Status;
    public String Role;

    public String Username;
    public String FullName;
    public String Address;
    public String BirthDate;
    public String PhoneNumber;

    public User(String username, String fullName, String address, String birthDate, String phoneNumber, String role) {
        Username = username;
        FullName = fullName;
        Address = address;
        BirthDate = birthDate;
        PhoneNumber = phoneNumber;
        Status = "idle";
        Role = role;
    }
}
