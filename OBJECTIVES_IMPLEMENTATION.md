# Project Objectives Implementation Mapping

This document outlines how each project objective has been implemented in the **Hospital Healthcare Management** system.

## 1. Design and normalize a relational database schema that models a hospital management domain effectively.
**Objective Overview:**  
This objective focuses on creating a structured database layout that minimizes redundancy (normalization) and accurately represents the relationships between hospital entities (Patients, Doctors, Appointments, etc.).

**Implementation Location:**  
- **File:** `src/schema.sql`

**Implementation Details:**  
The schema is designed with clear entities and relationships. Normalization is achieved through the use of Foreign Keys.
- **Entities:** `patients`, `doctors`, `departments`, `appointments`, `prescriptions`, `medical_inventory`.
- **Normalization Example:** `doctors` table references `departments(id)` instead of storing department strings repeatedly. `appointments` references `patients(id)` and `doctors(id)`.

**Code Example (`src/schema.sql` - Line 34):**
```sql
CREATE TABLE doctors (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    -- ...
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);
```

---

## 2. Develop conceptual, logical, and physical database models to ensure scalability and maintainability.
**Objective Overview:**  
This involves the actual translation of requirements into a physical SQL schema that can grow (scalability) and be easily updated (maintainability).

**Implementation Location:**  
- **File:** `src/schema.sql`
- **File:** `Hospital ER Diagram.drawio.png` (Visual Model)

**Implementation Details:**  
- **Scalability:** Use of `SERIAL` primary keys for auto-incrementing IDs.
- **Maintainability:** Modular table definitions and `DROP TABLE IF EXISTS` logic for easy reset/deployment.
- **Physical Model:** The SQL DDL statements in `schema.sql`.

---

## 3. Implement CRUD operations and complex queries using SQL and JDBC.
**Objective Overview:**  
Create, Read, Update, and Delete operations allow the application to manage data. Complex queries allow analytics (e.g., stats). JDBC is the Java API used to execute these SQL commands.

**Implementation Location:**  
- **Package:** `com.hospital.dao`
- **Files:** `PatientDAO.java`, `DoctorDAO.java`, `AppointmentDAO.java`, etc.

**Implementation Details:**  
- **CRUD:** Methods like `addPatient`, `getPatientById`, `updatePatient`, `deletePatient` in `PatientDAO`.
- **JDBC(Java Database Connectivity.):** Uses `PreparedStatement` to prevent SQL injection and efficiently execute queries.
- **Complex Query:** Aggregation for statistics.

**Code Example (`src/com/hospital/dao/PatientDAO.java` - Lines 13, 204):**
```java
// CREATE
public void addPatient(Patient patient) throws SQLException {
    String sql = "INSERT INTO patients (...) VALUES (?, ...)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, ...)) {
        // ...
        pstmt.executeUpdate();
    }
}

// COMPLEX QUERY (Aggregation)
public java.util.Map<String, Integer> getPatientsPerDayOfWeek() throws SQLException {
    String sql = "SELECT EXTRACT(DOW FROM created_at) as day_num, COUNT(*) as count " +
                 "FROM patients GROUP BY EXTRACT(DOW FROM created_at)";
    // ...
}
```

---

## 4. Apply indexing, hashing, searching, and sorting algorithms to optimize database access.
**Objective Overview:**  
Improves performance. Indexes speed up lookups. Searching allows filtering data.

**Implementation Location:**  
- **File:** `src/schema.sql` (Indexing)
- **File:** `src/com/hospital/dao/PatientDAO.java` (Searching)

**Implementation Details:**  
- **Indexing:** Explicit B-Tree indexes created on frequently searched columns (`last_name`, `specialization`).
- **Searching:** SQL `LIKE` operator used for flexible text search.

**Code Example (`src/schema.sql` - Line 58):**
```sql
-- Indexes for Optimization
CREATE INDEX idx_patients_name ON patients(first_name, last_name);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);
```

**Code Example (`src/com/hospital/dao/PatientDAO.java` - Line 131):**
```java
public List<Patient> searchPatients(String keyword) throws SQLException {
    String sql = "SELECT * FROM patients WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
    // ...
}
```


### Detailed Concepts

#### 1. Indexing
**Definition**: A "shortcut" system that lets the database find records instantly without scanning everything.

**Concept:** Imagine a textbook. If you want to find the chapter on "Hearts", you don't flip through every single page (which is slow). You go to the Index at the back, look up "Heart", and it gives you the exact page number.

**In Your Project:**
- **Creation (`src/schema.sql`):** The `CREATE INDEX` SQL commands are written in this file. They are executed **manually** (or via a database admin tool) when you first set up the database to prepare it for efficient queries. They are **not** called repeatedly by the Java application code.
- **Usage (Java DAOs):** Once created, these indexes are "used" automatically by the database whenever a relevant query is sent by our Java code:
  - `idx_patients_name`: effectively used by `PatientDAO.searchPatients()` (Line 131) which runs: `SELECT * ... WHERE first_name LIKE ?`.
  - `idx_doctors_specialization`: Prepared in `schema.sql` to optimize future filtering by doctor type.
  - `idx_appointments_date`: Prepared in `schema.sql` to optimize future calendar/date-range features.
- **Database Primary Keys:** In assignments, primary keys are automatically indexed. For example, `WHERE id=?` in `DoctorDAO.java` uses this automatic index for instant lookup.
- **Java Maps:** In `DoctorController.java`, you use:
``(61)``
  ```java
  private Map<Integer, String> departmentNameCache = new HashMap<>();
  ```
  Here, the Integer (Department ID) acts as an index. When you give the map an ID, it instantly gives you the Department Name, just like looking up a page number.

#### 2. Hashing
**Definition**: Converting data (like passwords) into a unique string of characters for security or efficient storage.

**Concept:** Hashing is like a digital fingerprint. You take a piece of data (like a password or a file) and run it through a mathematical formula (hash function) to get a unique string of characters. You cannot turn the fingerprint back into the original thumb, but every time you scan that thumb, you get the exact same fingerprint.

**In Your Project:**
- **Security:** In `com.hospital.util.PasswordUtil.java`, you use:
  ```java
  MessageDigest.getInstance("SHA-256");
  ```
  When a user creates a password like `secret123`, you don't save `secret123` in the database. You save its hash (e.g., `a5d3...`). When they try to log in, you hash their input and compare it to the stored hash. If they match, the password is correct.
- **Data Retrieval:** Every time you use a HashMap (like `patientCache` in `PatientService`), Java "hashes" the key (ID) to decide exactly where in memory to store the data for instant retrieval later.

#### 3. Searching
**Definition**: Finding specific data within a list or database.

**Concept:** The process of finding a specific item within a larger collection of data.

**In Your Project:**
- **Database Search (SQL):** 
  - **File:** `src/com/hospital/dao/PatientDAO.java`
  - **Line:** 133
  - **Code:** `String sql = "SELECT * FROM patients WHERE LOWER(first_name) LIKE ? OR ...";`
  - **Explanation:** This query runs when you type in the search bar. The database effectively uses the `idx_patients_name` index here to quickly locate patients matching the keyword.
- **Linear Search:** If you had a `List<Doctor>` and wrote a for loop to check every doctor one by one to find "Dr. Smith", that would be a Linear Search.
- **Binary Search:** In `MegaCorpSearchJson.java` (from your history), you implemented a Binary Search for managers. This is a much faster way to search sorted lists by repeatedly dividing the search interval in half.

#### 4. Sorting
**Definition**: Arranging data in order (e.g., newest to oldest).

**Concept:** Arranging data in a specific order (like A-Z, 1-100, or Oldest-Newest).

**In Your Project:**
- **SQL Sorting:** 
  - **File:** `src/com/hospital/dao/PrescriptionDAO.java`
  - **Line:** 69 (and 119, 179)
  - **Code:** `String sql = "SELECT * FROM prescriptions ... ORDER BY prescription_date DESC";`
  - **Explanation:** The `ORDER BY` clause forces the database to sort the results by date (newest first) before sending them to Java.
- **Java Sorting:** In `PerformanceController.java`, you use:
  ```java
  Comparator.comparingInt(PerformanceEntry::getDuration)
  ```
  This is a Java utility that defines how to compare two items so a sorting algorithm knows which one should come first in a list (e.g., sorting tasks by how long they took).

---

## 5. Integrate database operations into a JavaFX application interface for practical interaction.
**Objective Overview:**  
Connects the backend data logic to a frontend GUI so users can actually use the system.

**JavaFX:**
- JavaFX is a Java framework used to build modern desktop applications with graphical user interfaces (GUIs).

**FXML** 
- is an XML-based markup language used in JavaFX to design the user interface (UI) separately from the Java logic.

**Implementation Location:**  
- **Package:** `com.hospital.controller`
- **Package:** `com.hospital.view` (*.fxml files)

**Implementation Details:**  
- **Controllers:** `PatientController.java` calls `PatientDAO` to fetch data and populates `TableView`.
- **FXML:** Defines the structure (Tables, Buttons, Forms).

**Code Reference:**
- `PatientController.java` (Line 138): Calls `dao.getAllPatients()` and sets it to `patientTable.setItems(...)`.
- `PatientView.fxml` (Line 38): Contains `<TableView fx:id="patientTable">`.

---

## 6. Compare relational and NoSQL designs for unstructured data storage such as patient notes or medical logs.
**Objective Overview:**  
Demonstrates the use of a NoSQL database (MongoDB) for data that doesn't fit neatly into rows and columns, like dynamic patient notes with varying fields.

**Implementation Location:**  
- **File:** `src/com/hospital/dao/MongoNoteDAO.java`
- **File:** `src/com/hospital/model/PatientNote.java`

**Implementation Details:**  
- **NoSQL:** Uses MongoDB to store patient notes.
- **Unstructured Data:** The `PatientNote` model uses a `Map<String, String>` to store content, allowing any number of custom fields (e.g., "Vitals", "Observations") without changing the schema. This contrasts with the fixed schema in PostgreSQL.

**Code Example (`src/com/hospital/dao/MongoNoteDAO.java` - Line 45):**
```java
// Storing dynamic content in MongoDB Document
Document doc = new Document("_id", note.getId())
        .append("patientId", note.getPatientId());

if (note.getContent() != null) {
    Document contentDoc = new Document();
    for (Map.Entry<String, String> entry : note.getContent().entrySet()) {
        contentDoc.append(entry.getKey(), entry.getValue());
    }
    doc.append("content", contentDoc);
}
collection.insertOne(doc);
```

---

## 7. Measure and document performance improvement through optimization and indexing.
**Objective Overview:**  
Validates that the optimizations (indexes, NoSQL for specific use cases) actually work by measuring execution time.

**Implementation Location:**  
- **File:** `performance_report.csv`

**Implementation Details:**  
- The application tracks the duration of DB operations in milliseconds.
- Comparison can be made between operations (e.g., "DB Scan" vs specific lookups, or Relational vs NoSQL write speeds).

**Evidence from `performance_report.csv`:**
```csv
Timestamp,Operation,Duration_ms
2026-01-15T09:15:13.848379300,getAllAppointments,59
2026-01-15T09:16:31.790646400,getPatientNotes (MongoDB),987
```
(Note: Real-time logging of these metrics allows for performance tuning).
