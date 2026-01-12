# Hospital Management System

A comprehensive, JavaFX-based Hospital Management System backed by a PostgreSQL database. Designed for efficient management of hospital operations including patients, staff, appointments, inventory, and prescriptions.

## Key Features

### 🏥 Dashboard & UI
- **Interactive Dashboard**: Real-time counters for Patients, Doctors, and Appointments.
- **Quick Actions**: One-click access to common tasks like Patient Registration.
- **Universal Navigation**: Smooth navigation with a history-aware "Back" button.

### 👥 Patient Management
- **Registration**: Complete patient profiles with validation.
- **Search**: Fast, indexed search by name or ID.
- **CRUD Operations**: Update and delete patient records easily.

### 👨‍⚕️ Staff & Scheduling
- **Doctor Management**: Specialized module for managing medical staff.
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

## Prerequisites

Before setting up the application, ensure you have the following installed:

### 1. Java Development Kit (JDK)
- **Version**: Java 21 or higher
- **Download**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- **Verify installation**:
  ```powershell
  java -version
  javac -version
  ```

### 2. PostgreSQL Database
- **Version**: PostgreSQL 12 or higher
- **Download**: [PostgreSQL Official Site](https://www.postgresql.org/download/)
- **Installation**: Follow the installer wizard and remember the password you set for the `postgres` user
- **Verify installation**:
  ```powershell
  psql --version
  ```

### 3. JavaFX SDK
- **Version**: JavaFX SDK 25.0.1
- **Download**: [Gluon JavaFX](https://gluonhq.com/products/javafx/)
- **Important**: Download the SDK (not jmods) for your operating system
- **Extract** the downloaded archive to the `lib/` folder in the project directory

### 4. PostgreSQL JDBC Driver
- **Version**: postgresql-42.7.8.jar (or compatible)
- **Download**: [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/download/)
- **Place** the JAR file in the `lib/` folder

## Setup Instructions

### Step 1: Clone or Download the Project
```powershell
git clone <repository-url>
cd "Hospital Healthcare Management"
```

### Step 2: Set Up Dependencies
Ensure your `lib/` folder structure looks like this:
```
lib/
├── postgresql-42.7.8.jar
└── javafx-sdk-25.0.1/
    └── lib/
        ├── javafx.base.jar
        ├── javafx.controls.jar
        ├── javafx.fxml.jar
        └── ... (other JavaFX libraries)
```

### Step 3: Database Configuration

#### 3.1 Create the Database
Open a terminal/PowerShell and run:
```powershell
# Connect to PostgreSQL (you'll be prompted for the postgres user password)
psql -U postgres

# Create the database
CREATE DATABASE hospital_db;

# Exit psql
\q
```

#### 3.2 Execute the Schema Script
```powershell
psql -U postgres -d hospital_db -f src/schema.sql
```

#### 3.3 Configure Database Credentials
Open `src/com/hospital/util/DBUtil.java` and update the connection details:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
private static final String USER = "postgres";  // Change if using a different user
private static final String PASSWORD = "your_password_here";  // Update with your PostgreSQL password
```

> [!IMPORTANT]
> **Security Note**: The database password is currently hardcoded. For production use, consider using environment variables or a configuration file that's excluded from version control.

### Step 4: Compilation

Create a `bin` directory if it doesn't exist, then compile:

```powershell
# Create bin directory
mkdir bin -Force

# Generate list of all Java source files
dir /s /b *.java > sources.txt

# Compile the project
javac -d bin --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml -cp "lib/postgresql-42.7.8.jar;src" @sources.txt
```

### Step 5: Run the Application

```powershell
java -cp "bin;lib/postgresql-42.7.8.jar" --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml com.hospital.Main
```

## Project Structure

```
Hospital Healthcare Management/
├── src/
│   ├── com/hospital/
│   │   ├── model/          # Data models (POJOs)
│   │   ├── dao/            # Data Access Objects for database interaction
│   │   ├── service/        # Business logic layer
│   │   ├── controller/     # JavaFX controllers handling UI logic
│   │   ├── util/           # Utility classes (Database connection, etc.)
│   │   └── Main.java       # Application entry point
│   ├── com/hospital/view/  # FXML files and CSS styles
│   └── schema.sql          # Database schema
├── lib/                    # External libraries
├── bin/                    # Compiled classes (generated)
└── README.md
```

## Troubleshooting

### Common Issues and Solutions

#### 1. `Error: JavaFX runtime components are missing`
**Cause**: JavaFX SDK not found or incorrect path.

**Solution**:
- Verify JavaFX SDK is extracted in `lib/javafx-sdk-25.0.1/`
- Check the `--module-path` in the run command matches your JavaFX location
- Ensure you downloaded the SDK, not jmods

#### 2. `java.sql.SQLException: Connection refused`
**Cause**: PostgreSQL server is not running or wrong connection details.

**Solution**:
- Start PostgreSQL service:
  ```powershell
  # Windows (as Administrator)
  net start postgresql-x64-<version>
  ```
- Verify PostgreSQL is running on port 5432
- Check `DBUtil.java` has correct URL, username, and password
- Ensure `hospital_db` database exists

#### 3. `ClassNotFoundException: org.postgresql.Driver`
**Cause**: PostgreSQL JDBC driver not in classpath.

**Solution**:
- Verify `postgresql-42.7.8.jar` exists in `lib/` folder
- Check the `-cp` parameter includes the correct JAR filename
- Ensure the JAR file is not corrupted (re-download if needed)

#### 4. `PSQLException: FATAL: password authentication failed`
**Cause**: Incorrect database credentials.

**Solution**:
- Update the `PASSWORD` field in `DBUtil.java` with your PostgreSQL password
- Verify the `USER` field matches your PostgreSQL username (default: `postgres`)

#### 5. Compilation Errors
**Cause**: Java version mismatch or missing dependencies.

**Solution**:
- Ensure Java 21+ is installed: `java -version`
- Delete `bin/` folder and recompile
- Check all `.java` files are included in `sources.txt`

#### 6. `ERROR: relation "patients" does not exist`
**Cause**: Database schema not created.

**Solution**:
- Execute the schema script:
  ```powershell
  psql -U postgres -d hospital_db -f src/schema.sql
  ```
- Verify tables were created:
  ```powershell
  psql -U postgres -d hospital_db -c "\dt"
  ```

## Performance Optimizations

- **Caching**: Implemented LRU Cache for frequent patient lookups used in the dashboard
- **Indexing**: Database indexes on `last_name` and `specialization` for fast search
- **Connection Pooling**: Consider implementing connection pooling for production use

## Contributing

When contributing to this project:
1. Never commit `DBUtil.java` with real passwords
2. Test all database operations thoroughly
3. Follow the existing code structure and naming conventions
4. Update this README if you add new features or dependencies

## License

This project is for educational purposes.