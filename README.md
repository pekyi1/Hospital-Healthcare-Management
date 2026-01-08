# Hospital Management System

A JavaFX-based Hospital Management System with PostgreSQL backend.

## Prerequisites
- Java JDK 11+
- PostgreSQL 12+
- JavaFX SDK (if not bundled with JDK)

## Database Setup
1. Create a database named `hospital_db`.
2. Run the schema script `src/schema.sql` to create tables and insert sample data.
   ```bash
   psql -U postgres -d hospital_db -f src/schema.sql
   ```
   *(Ensure credentials in `src/com/hospital/util/DBUtil.java` match your local setup: User `postgres`, Password `password`)*

## Project Structure
- `com.hospital.model`: POJOs (Patient, Doctor, etc.)
- `com.hospital.dao`: Database Access Objects
- `com.hospital.service`: Business Logic & Caching
- `com.hospital.view`: FXML Layouts
- `com.hospital.controller`: JavaFX Controllers
- `com.hospital.util`: Database Connection Utility

## Running the Application
Compile and run `com.hospital.Main`.
```bash
javac -d out --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml src/com/hospital/**/*.java
java -cp out --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml com.hospital.Main
```
*(Adjust paths to your JavaFX SDK)*

## Features
- **Patients**: Add, Edit, Delete, Search (Indexed).
- **Doctors**: Manage doctor records.
- **Appointments**: Schedule and view appointments.
- **Optimization**: In-memory caching for performance.