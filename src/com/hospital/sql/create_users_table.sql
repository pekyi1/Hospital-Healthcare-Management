-- Hospital Management System - Users Table
-- Run this script in PostgreSQL to create the users table and seed the admin account

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('Admin', 'Doctor', 'Patient')),
    reference_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Seed default admin account
-- Password: admin123 (SHA-256 hashed)
INSERT INTO users (username, password_hash, role, is_active)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Note: To create doctor/patient accounts, use the following format:
-- INSERT INTO users (username, password_hash, role, reference_id, is_active)
-- VALUES ('doctor_username', 'hashed_password', 'Doctor', <doctor_id_from_doctors_table>, TRUE);
