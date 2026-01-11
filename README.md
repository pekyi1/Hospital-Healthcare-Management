# Hospital Management System

A comprehensive, JavaFX-based Hospital Management System backed by a PostgreSQL database. Designed for efficient management of hospital operations including patients, staff, appointments, inventory, and prescriptions.

## Key Features

### 🏥 Dashboard & UI
- **Interactive Dashboard**: Real-time counters for Patients, Doctors, and Appointments.
- **Quick Actions**: One-click access to common tasks like Patient Registration.
- **Universal Navigation**: Smooth navigation with a history-aware "Back" button.

### 👥 Patient Management
- **Registration**: Complete patient profiles with validation.
- **Search**: fast, indexed search by name or ID.
- **CRUD Operations**: Update and delete patient records easily.

### 👨‍⚕️ Staff & Scheduling
- **Doctor Management**: specialized module for managing medical staff.
- **Appointments**: Schedule, view, and manage patient appointments.

### 💊 Medical & Feedback
- **Inventory System**: Track medical supplies and equipment.
- **Prescriptions**: Digital prescription management linked to patients and doctors.
- **Patient Feedback**: System for collecting and reviewing patient satisfaction.

## Technology Stack

- **Frontend**: JavaFX 25 (FXML based UI)
- **Backend**: Java 21+ with JDBC
- **Database**: PostgreSQL 12+
- **Styling**: CSS for a modern, clean look

## Setup Instructions

### 1. Database Configuration
1. Create a PostgreSQL database named `hospital_db`.
2. Execute the schema script located at `src/schema.sql` to set up tables and indexes.
   ```bash
   psql -U postgres -d hospital_db -f src/schema.sql
   ```
3. Verify your database credentials in `src/com/hospital/util/DBUtil.java`.

### 2. Dependencies
Ensure you have the following libraries in your `lib/` folder:
- `postgresql-42.7.4.jar` (or compatible version)
- `javafx-sdk-25.0.1` (extracted, not just a jar)

### 3. Compilation & Running
Use the provided PowerShell command to compile and run the application.

**Compile:**
```powershell
dir /s /b *.java > sources.txt
javac -d bin --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml -cp "lib/postgresql-42.7.4.jar;src" @sources.txt
```

**Run:**
```powershell
java -cp "bin;lib/postgresql-42.7.4.jar" --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml com.hospital.Main
```

## Project Structure
- `com.hospital.model`: Data models (POJOs).
- `com.hospital.dao`: Data Access Objects for database interaction.
- `com.hospital.service`: Business logic layer.
- `com.hospital.view`: FXML files and CSS styles.
- `com.hospital.controller`: JavaFX controllers handling UI logic.
- `com.hospital.util`: Utility classes (Database connection, etc.).

## Performance
- **Caching**: Implemented LRU Cache for frequent patient lookups used in the dashboard.
- **Indexing**: Database indexes on `last_name` and `specialization` for fast search.