-- Database Schema for Hospital Management System

-- Drop tables if they exist
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS departments;

-- Create Departments Table
CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

-- Create Patients Table
CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    birth_date DATE,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT
);

-- Create Doctors Table
CREATE TABLE doctors (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Create Appointments Table
CREATE TABLE appointments (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'Scheduled',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- Indexes for Optimization
CREATE INDEX idx_patients_name ON patients(first_name, last_name);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);

-- Insert Sample Data
INSERT INTO departments (name, location) VALUES 
('Cardiology', 'Building A, Floor 1'),
('Neurology', 'Building A, Floor 2'),
('Pediatrics', 'Building B, Floor 1');

INSERT INTO patients (first_name, last_name, gender, birth_date, email, phone, address) VALUES
('John', 'Doe', 'Male', '1985-05-15', 'john.doe@example.com', '123-456-7890', '123 Main St'),
('Jane', 'Smith', 'Female', '1990-08-20', 'jane.smith@example.com', '987-654-3210', '456 Oak Ave');

INSERT INTO doctors (first_name, last_name, specialization, email, phone, department_id) VALUES
('Alice', 'Williams', 'Cardiologist', 'alice.w@hospital.com', '555-0101', 1),
('Bob', 'Johnson', 'Neurologist', 'bob.j@hospital.com', '555-0102', 2);
