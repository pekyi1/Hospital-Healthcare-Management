-- Database Schema for Hospital Management System

-- Drop tables if they exist (Order matters for Foreign Keys)
DROP TABLE IF EXISTS prescription_items;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS patient_feedback;
DROP TABLE IF EXISTS medical_inventory;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

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

-- Create Medical Inventory Table
CREATE TABLE medical_inventory (
    id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    quantity INT DEFAULT 0,
    unit_price DECIMAL(10, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Prescriptions Table
CREATE TABLE prescriptions (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT,
    prescription_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- Create Prescription Items Table
CREATE TABLE prescription_items (
    id SERIAL PRIMARY KEY,
    prescription_id INT NOT NULL,
    inventory_id INT NOT NULL,
    quantity INT NOT NULL,
    dosage_instructions VARCHAR(200),
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id),
    FOREIGN KEY (inventory_id) REFERENCES medical_inventory(id)
);

-- Create Patient Feedback Table
CREATE TABLE patient_feedback (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- Additional Indexes
CREATE INDEX idx_inventory_name ON medical_inventory(item_name);
CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id);

-- Sample Data for Inventory
INSERT INTO medical_inventory (item_name, category, quantity, unit_price) VALUES
('Paracetamol', 'Medicine', 500, 5.00),
('Amoxicillin', 'Antibiotic', 200, 15.50),
('Bandage', 'Supply', 100, 2.00);


-- Create Users Table (Authentication)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('Admin', 'Doctor', 'Patient')),
    reference_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Indexes for Users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- Seed Default Admin Account
-- Password: admin123 (SHA-256 hashed)
INSERT INTO users (username, password_hash, role, is_active)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin', TRUE);
-- Seed Default Doctor Account
INSERT INTO users (username, password_hash, role, is_active)
VALUES ('doctor', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Doctor', TRUE);


