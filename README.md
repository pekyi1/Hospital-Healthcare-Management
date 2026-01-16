# Hospital Management System

A comprehensive, JavaFX-based Hospital Management System backed by a PostgreSQL database. Designed for efficient management of hospital operations including patients, staff, appointments, inventory, and prescriptions.

## Key Features

### 🏥 Dashboard & UI
- **Interactive Dashboard**: Real-time counters for Patients, Doctors, and Appointments.
- **Quick Actions**: One-click access to common tasks like Patient Registration.
- **Universal Navigation**: Smooth navigation with a history-aware "Back" button.

### 🔐 Authentication & Role-Based Access
- **Role-Selection Login**: Choose your role (Admin, Doctor, or Patient) on the login screen
- **Credential-Based Access**: Admin and Doctor roles require username/password authentication
- **Patient View**: Direct access for patients to submit feedback and view prescriptions
- **Three User Roles with Specific Permissions**:
  - **Admin**: Full system access to all modules and functionality
  - **Doctor**: Clinical access (Dashboard, Patients, Doctors, Appointments, Prescriptions, Feedback)
  - **Patient**: Limited self-service access:
    - ✅ Submit feedback (cannot view feedback table)
    - ✅ View prescriptions (cannot add/edit/delete)
- **Session Management**: Secure sign-in/sign-out with window state preservation

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
- **Patient Feedback**: 
  - Patients can submit feedback about their experience
  - Admins and Doctors can view all submitted feedback

### 📝 Patient Notes (NoSQL)
- **Cloud-Stored Notes**: Patient notes stored in MongoDB Atlas (cloud NoSQL database).
- **Unstructured Data**: Flexible storage for nurse logs, vitals, medical history, etc.
- **Real-Time Sync**: Notes persist instantly to the cloud.

## Technology Stack

- **Frontend**: JavaFX 25 (FXML based UI)
- **Backend**: Java 21+ with JDBC
- **Relational Database**: PostgreSQL 12+
- **NoSQL Database**: MongoDB Atlas (Cloud)
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

### 5. MongoDB Java Driver (for Patient Notes)
- **Version**: 4.11.1
- **Download** the following JAR files from [Maven Central](https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync/4.11.1):
  - `bson-4.11.1.jar`
  - `mongodb-driver-core-4.11.1.jar`
  - `mongodb-driver-sync-4.11.1.jar`
- **Place** all three JAR files in the `lib/` folder

### 6. MongoDB Atlas Account (Free)
- **Sign up** at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)
- Create a free cluster and obtain your connection string
- Update the connection string in `src/com/hospital/dao/MongoNoteDAO.java`

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
├── bson-4.11.1.jar
├── mongodb-driver-core-4.11.1.jar
├── mongodb-driver-sync-4.11.1.jar
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

#### 3.4 Set Up User Authentication Table
Execute the users table script to enable login functionality:
```powershell
psql -U postgres -d hospital_db -f src/com/hospital/sql/create_users_table.sql
```

This creates the `users` table and seeds default accounts:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor` | `admin123` |

> [!NOTE]
> **Patient View** does not require login credentials - patients can access their limited view directly from the login screen.

> [!WARNING]
> **Change Default Passwords**: After deployment, change the default passwords for security.


### Step 4: Compilation (Recommended Method)

Use the provided PowerShell script for easy compilation and execution:

```powershell
.\compile.ps1
```

This script automatically:
- Compiles all Java files with correct module paths
- Includes all dependencies (PostgreSQL, MongoDB, JavaFX)
- Copies FXML/CSS resources
- Launches the application

### Step 4 (Alternative): Manual Compilation

```powershell
# Create bin directory
mkdir bin -Force

# Compile the project
javac -d bin --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules "javafx.controls,javafx.fxml" -cp "lib/postgresql-42.7.8.jar;lib/mongodb-driver-sync-4.11.1.jar;lib/mongodb-driver-core-4.11.1.jar;lib/bson-4.11.1.jar;src" @sources.txt
```

### Step 5: Run the Application

```powershell
java -cp "bin;lib/postgresql-42.7.8.jar;lib/mongodb-driver-sync-4.11.1.jar;lib/mongodb-driver-core-4.11.1.jar;lib/bson-4.11.1.jar" --module-path "lib/javafx-sdk-25.0.1/lib" --add-modules "javafx.controls,javafx.fxml" com.hospital.Main
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

- **Caching**: Implemented HashMap-based in-memory caching for frequent patient/doctor lookups
- **Indexing**: Database indexes on `last_name`, `specialization`, and `appointment_date` for fast search
- **Performance Logging**: All database operations are logged to `performance_report.csv` with execution times
- **NoSQL for Unstructured Data**: Patient notes stored in MongoDB for flexible schema and fast document retrieval

## Contributing

When contributing to this project:
1. Never commit `DBUtil.java` with real passwords
2. Test all database operations thoroughly
3. Follow the existing code structure and naming conventions
4. Update this README if you add new features or dependencies

## License

This project is for educational purposes.