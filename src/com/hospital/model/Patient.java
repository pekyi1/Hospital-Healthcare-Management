package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {
    private int id;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public Patient() {
    }

    public Patient(int id, String firstName, String lastName, String gender, LocalDate birthDate, String email,
            String phone, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Patient(int id, String firstName, String lastName, String gender, LocalDate birthDate, String email,
            String phone, String address, LocalDateTime createdAt) {
        this(id, firstName, lastName, gender, birthDate, email, phone, address);
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
