# Test Scenarios - Hospital Healthcare Management System

## Document Information
- **Project**: Hospital Healthcare Management System
- **Purpose**: Test database manipulation through the JavaFX application
- **Version**: 3.0
- **Last Updated**: January 13, 2026

---

## Test Environment Setup

### Prerequisites
- PostgreSQL database server running
- Database `hospital_db` created and schema loaded
- JavaFX application compiled and running
- Test data: At least 5 patients, 3 doctors, 3 departments

---

## Database Manipulation Test Scenarios

### Test Scenario 1: Patient CRUD Operations

**Test ID**: TS-DB-001  
**Priority**: Critical  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Add New Patient**
   - Open Patient Management in the app
   - Click "+ Add Patient"
   - Fill in: First Name: "John", Last Name: "Doe", DOB: "1990-05-15", Gender: "Male", Phone: "555-1234", Email: "john.doe@email.com"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT * FROM patients WHERE email = 'john.doe@email.com';
     ```
   - **Expected**: Record exists in database with all entered data

2. **READ - View Patient Data**
   - Search for "John Doe" in the search bar
   - Verify displayed data matches database record
   - **Expected**: App displays data correctly from database

3. **UPDATE - Modify Patient**
   - Select "John Doe" and click "Edit"
   - Change Phone to "555-9999"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT phone FROM patients WHERE email = 'john.doe@email.com';
     ```
   - **Expected**: Phone number updated to "555-9999" in database

4. **DELETE - Remove Patient**
   - Select "John Doe" and click "Del"
   - Confirm deletion
   - Verify in database:
     ```sql
     SELECT * FROM patients WHERE email = 'john.doe@email.com';
     ```
   - **Expected**: Record no longer exists in database

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 2: Doctor CRUD Operations with Department

**Test ID**: TS-DB-002  
**Priority**: Critical  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Add New Doctor with Specialization AND Department**
   - Open Doctor Management
   - Click "+ Add Doctor"
   - Fill in: First Name: "Sarah", Last Name: "Johnson", Specialization: "Cardiologist"
   - Select Department from dropdown: "Cardiology"
   - Fill in: Email: "sarah.j@hospital.com", Phone: "555-5678"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT d.*, dept.name as department_name 
     FROM doctors d 
     JOIN departments dept ON d.department_id = dept.id 
     WHERE d.last_name = 'Johnson';
     ```
   - **Expected**: Doctor record created with both specialization AND department_id linked

2. **READ - View Doctor with Department Name**
   - View Doctor table
   - **Expected**: Table displays both "Specialization" column and "Department" column with actual names (not IDs)

3. **UPDATE - Change Specialization and Department**
   - Select "Sarah Johnson" and click "Edit"
   - Change Specialization to "Neurologist"
   - Change Department dropdown to "Neurology"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT specialization, department_id FROM doctors WHERE last_name = 'Johnson';
     ```
   - **Expected**: Both specialization AND department_id updated

4. **DELETE - Remove Doctor**
   - Select "Sarah Johnson" and click "Del"
   - Confirm deletion
   - Verify in database:
     ```sql
     SELECT * FROM doctors WHERE last_name = 'Johnson';
     ```
   - **Expected**: Doctor record deleted from database

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 3: Doctor Search Functionality

**Test ID**: TS-DB-003  
**Priority**: High  
**Test Type**: UI Feature Testing

**Test Steps**:

1. **Search by First Name**
   - Open Doctor Management
   - Type "Alice" in search bar and press Enter
   - **Expected**: Only doctors with "Alice" in first name are displayed

2. **Search by Last Name**
   - Type "Williams" in search bar and press Enter
   - **Expected**: Only doctors with "Williams" in last name are displayed

3. **Search by Specialization**
   - Type "Cardiologist" in search bar and press Enter
   - **Expected**: Only doctors with "Cardiologist" specialization are displayed

4. **Search by Email**
   - Type "@hospital.com" in search bar and press Enter
   - **Expected**: Only doctors with "@hospital.com" in email are displayed

5. **Clear Search**
   - Clear search field and press Enter
   - **Expected**: All doctors are displayed again

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 4: Department CRUD Operations

**Test ID**: TS-DB-004  
**Priority**: High  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Add Department**
   - Open Department Management (sidebar → Management → Departments)
   - Click "+ Add Department"
   - Enter Name: "Oncology", Location: "Building C, Floor 2"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT * FROM departments WHERE name = 'Oncology';
     ```
   - **Expected**: Department created in database with name and location

2. **READ - View Departments**
   - View Departments table
   - **Expected**: Table shows ID, Name, Location columns with all departments

3. **UPDATE - Modify Department**
   - Select "Oncology" department and click "Edit"
   - Change Location to "Building D, Floor 1"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT location FROM departments WHERE name = 'Oncology';
     ```
   - **Expected**: Location updated in database

4. **DELETE - Remove Department**
   - Select "Oncology" department and click "Del"
   - Confirm deletion
   - Verify in database:
     ```sql
     SELECT * FROM departments WHERE name = 'Oncology';
     ```
   - **Expected**: Department deleted from database

5. **VERIFY - Department appears in Doctor dropdown**
   - Create a new department "Dermatology"
   - Open Add Doctor dialog
   - Check Department dropdown
   - **Expected**: "Dermatology" appears in the dropdown list

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 5: Appointment Management with Name Lookup

**Test ID**: TS-DB-005  
**Priority**: High  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Schedule Appointment using Patient/Doctor Names**
   - Open Appointment Management
   - Click "+ Add Appointment"
   - Enter Patient First Name: "Alice", Patient Last Name: "Williams"
   - Enter Doctor First Name: "Bob", Doctor Last Name: "Johnson"
   - Select Date and Time, Status: "Scheduled"
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT a.*, p.first_name as patient_name, d.first_name as doctor_name 
     FROM appointments a 
     JOIN patients p ON a.patient_id = p.id 
     JOIN doctors d ON a.doctor_id = d.id 
     WHERE a.id = (SELECT MAX(id) FROM appointments);
     ```
   - **Expected**: Appointment created with correct patient_id and doctor_id from name lookup

2. **VALIDATION - Invalid Patient Name**
   - Click "+ Add Appointment"
   - Enter Patient First Name: "NonExistent", Patient Last Name: "Person"
   - Click "Save"
   - **Expected**: Error message "Patient Not Found" displayed, save prevented

3. **VALIDATION - Invalid Doctor Name**
   - Enter valid patient name
   - Enter Doctor First Name: "FakeDoctor", Doctor Last Name: "Name"
   - Click "Save"
   - **Expected**: Error message "Doctor Not Found" displayed, save prevented

4. **READ - View Appointments with Names**
   - View Appointments table
   - **Expected**: Table displays "Patient" and "Doctor" columns showing names (e.g., "Alice Williams") NOT IDs

5. **UPDATE - Reschedule Appointment**
   - Select appointment and click "Edit"
   - Change date/time
   - Click "Save"
   - Verify appointment date updated in database
   - **Expected**: Appointment date updated successfully

6. **DELETE - Cancel Appointment**
   - Select appointment and click "Del"
   - Confirm dialog shows patient and doctor NAMES
   - Confirm cancellation
   - **Expected**: Appointment deleted from database

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 6: Prescription Management

**Test ID**: TS-DB-006  
**Priority**: High  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Add Prescription**
   - Open Prescription Management
   - Click "Add Prescription"
   - Select Patient, Doctor, Medication, Dosage, Instructions
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT * FROM prescriptions WHERE patient_id = [patient_id];
     ```
   - **Expected**: Prescription created in database

2. **UPDATE - Modify Prescription**
   - Select prescription and click "Edit"
   - Change dosage or instructions
   - Click "Save"
   - Verify changes in database
   - **Expected**: Prescription updated in database

3. **DELETE - Remove Prescription**
   - Select prescription and click "Delete"
   - Confirm deletion
   - Verify removed from database
   - **Expected**: Prescription deleted

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 7: Medical Inventory Management

**Test ID**: TS-DB-007  
**Priority**: Medium  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Add Inventory Item**
   - Open Medical Inventory
   - Click "Add Item"
   - Fill in: Item Name, Quantity, Unit Price
   - Click "Save"
   - Verify in database:
     ```sql
     SELECT * FROM medical_inventory WHERE item_name = '[entered_name]';
     ```
   - **Expected**: Inventory item created in database

2. **UPDATE - Adjust Quantity**
   - Select item and click "Edit"
   - Change quantity
   - Click "Update"
   - Verify in database that quantity is updated
   - **Expected**: Quantity updated in database

3. **DELETE - Remove Item**
   - Select item and click "Delete"
   - Confirm deletion
   - Verify removed from database
   - **Expected**: Item deleted from database

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 8: Patient Feedback

**Test ID**: TS-DB-008  
**Priority**: Medium  
**Test Type**: Database Manipulation via Application

**Test Steps**:

1. **CREATE - Submit Feedback**
   - Open Patient Feedback
   - Click "Add Feedback"
   - Select Patient, enter Rating and Comments
   - Click "Submit"
   - Verify in database:
     ```sql
     SELECT * FROM patient_feedback WHERE patient_id = [patient_id];
     ```
   - **Expected**: Feedback record created in database

2. **READ - View Feedback**
   - View feedback list in app
   - Verify data matches database records
   - **Expected**: App displays feedback from database correctly

3. **DELETE - Remove Feedback**
   - Select feedback and click "Delete"
   - Confirm deletion
   - Verify removed from database
   - **Expected**: Feedback deleted from database

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 9: Role-Based Sidebar Visibility

**Test ID**: TS-UI-001  
**Priority**: Medium  
**Test Type**: UI Feature Testing

**Test Steps**:

1. **Admin Role - Full Access**
   - Click "Admin" tab at top
   - **Expected**: All sidebar buttons visible: Overview, Patients, Doctors, Appointments, Departments, Medical Inventory, Prescriptions, Performance, Feedbacks

2. **Doctor Role - Limited Access**
   - Click "Doctor" tab at top
   - **Expected**: Hidden: Departments, Medical Inventory, Performance
   - **Visible**: Overview, Patients, Appointments, Prescriptions, Feedbacks

3. **Patient Role - Restricted Access**
   - Click "Patient" tab at top
   - **Expected**: Hidden: Overview, Patients, Departments, Medical Inventory, Performance
   - **Visible**: Doctors, Appointments, Prescriptions

**Status**: ⬜ Pass / ⬜ Fail

---

### Test Scenario 10: Referential Integrity Testing

**Test ID**: TS-DB-009  
**Priority**: High  
**Test Type**: Database Constraint Validation

**Test Steps**:

1. **Test Foreign Key Constraint - Appointment**
   - Try to create appointment with non-existent patient_id via direct database insert:
     ```sql
     INSERT INTO appointments (patient_id, doctor_id, appointment_date) 
     VALUES (99999, 1, CURRENT_DATE);
     ```
   - **Expected**: Database rejects insert with foreign key violation error

2. **Test Doctor-Department Relationship**
   - Create a doctor assigned to a department
   - Try to delete that department
   - **Expected**: Deletion fails or cascades appropriately

3. **Test NOT NULL Constraint**
   - Try to save patient without required field (e.g., last name) via app
   - **Expected**: App validation prevents save, or database rejects with NOT NULL error

4. **Test Cascade Delete**
   - Create a patient with appointments
   - Delete patient via app
   - Verify in database:
     ```sql
     SELECT * FROM appointments WHERE patient_id = [deleted_patient_id];
     ```
   - **Expected**: Related appointments are handled per cascade rules

**Status**: ⬜ Pass / ⬜ Fail

---

## Test Summary

| Test Scenario | Status | Notes |
|---------------|--------|-------|
| TS-DB-001: Patient CRUD | ⬜ Pass / ⬜ Fail | |
| TS-DB-002: Doctor CRUD with Department | ⬜ Pass / ⬜ Fail | |
| TS-DB-003: Doctor Search | ⬜ Pass / ⬜ Fail | |
| TS-DB-004: Department CRUD | ⬜ Pass / ⬜ Fail | |
| TS-DB-005: Appointment with Names | ⬜ Pass / ⬜ Fail | |
| TS-DB-006: Prescription Management | ⬜ Pass / ⬜ Fail | |
| TS-DB-007: Inventory Management | ⬜ Pass / ⬜ Fail | |
| TS-DB-008: Patient Feedback | ⬜ Pass / ⬜ Fail | |
| TS-UI-001: Role-Based Sidebar | ⬜ Pass / ⬜ Fail | |
| TS-DB-009: Referential Integrity | ⬜ Pass / ⬜ Fail | |

### Overall Result
- **Total Tests**: 10
- **Passed**: ___
- **Failed**: ___
- **Pass Rate**: ___%

### Critical Issues
_[List any issues found during testing]_

---

## Quick Database Verification Commands

### Check Record Counts
```sql
SELECT 'Patients' as table_name, COUNT(*) as count FROM patients
UNION ALL
SELECT 'Doctors', COUNT(*) FROM doctors
UNION ALL
SELECT 'Departments', COUNT(*) FROM departments
UNION ALL
SELECT 'Appointments', COUNT(*) FROM appointments
UNION ALL
SELECT 'Prescriptions', COUNT(*) FROM prescriptions;
```

### View Recent Changes
```sql
-- Most recently added patients
SELECT * FROM patients ORDER BY id DESC LIMIT 5;

-- Most recently added doctors with departments
SELECT d.*, dept.name as department_name 
FROM doctors d 
LEFT JOIN departments dept ON d.department_id = dept.id 
ORDER BY d.id DESC LIMIT 5;

-- Recent appointments
SELECT * FROM appointments ORDER BY id DESC LIMIT 5;
```

### Verify Constraints
```sql
-- List all foreign keys
SELECT tc.table_name, kcu.column_name, ccu.table_name AS foreign_table
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';
```

---

**End of Test Scenarios Document**
