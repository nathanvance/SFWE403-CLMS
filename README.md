Semester Project — SFWE 403 / 503
University of Arizona • Fall 2025

## Project Repository  
Full source code available at: https://github.com/nathanvance/SFWE403-CLMS

This repository contains the full source code for our semester-long group project: 
A Clinical Laboratory & Pharmacy Management System built using Java, JavaFX, and Maven.

The system implements role-based authentication, secure access control, drug inventory tracking, 
pharmacist/admin workflows, low-stock alerts, login lockout protection, patient testing workflows, 
and persistent data storage via CSV/JSON files.

-----------------------------------------------------------------------------------------------------

## 1. Repository Contents

**Top-level structure**

- `/src/main/java/`  
  - `controllers/` – JavaFX controllers that handle UI events and screen logic  
    - `LoginController.java` – handles login, authentication, and role-based routing  
    - `AdminDashboardController.java` – admin view for managing users and inventory  
    - `PharmacistDashboardController.java` – pharmacist view for requests and approvals  
    - `PatientDashboardController.java` – patient view
    - `InventoryController.java` – manages stock levels, low-stock alerts, and requests  
    - `TestEntryFormController.java` – form for entering lab test / patient results
  - `model/` – core domain classes  
    - `User.java` – base user model with id, name, role, and credentials  
    - `Admin.java`, `Pharmacist.java`, `Patient.java` – role-specific user types  
    - `InventoryItem.java` – represents a drug / stock item with quantity and thresholds  
    - `InventoryRequest.java` – pharmacist request for items from inventory  
    - `TestEntry.java` – patient test record and results
  - `persistence/` – data access and storage  
    - `JsonStorage.java` – reads/writes JSON files for persistent entities  
    - `CsvStorage.java` – helper for importing or logging CSV data  
    - `DataInitializer.java` – boot-up logic to load data from JSON/CSV into memory
  - `security/` – authentication and security utilities  
    - `AuthManager.java` – login verification, lockout logic, and session management  
    - `PasswordUtils.java` – hashing, salting, and password checks
  - `App.java` / `Main.java` – JavaFX application entry point; boots the UI

- `/src/main/resources/`  
  - `/fxml/` – JavaFX layout files  
    - `login.fxml` – login screen  
    - `adminDashboard.fxml` – admin dashboard  
    - `pharmacistDashboard.fxml` – pharmacist dashboard  
    - `patientDashboard.fxml` – patient dashboard  
    - `inventoryDashboard.fxml` – inventory management view  
    - `TestEntryForm.fxml` – test entry form UI  
  - `/css/` – optional CSS styles for the JavaFX views  
  - `/images/` – logos, icons, or other UI graphics

- `/data/` – application data files used for persistence  
  - `user_pass.csv` – user credentials (username, hashed password, role)  
  - `login_failures.csv` – failed login attempts for lockout logic  
  - `patient_data.json` – patient demographics and test history  
  - `inventory_requests.json` – pending and approved inventory requests  
  - `drug_changes.json` – log of medication or stock updates  
  - `admin_notifications.log` – audit / notification log  
  - `TestEntries.csv` or `TestEntries.json` – stored test results  
  - `failures_file.csv` – error/debug logging

- Build files  
  - `pom.xml` – Maven configuration (dependencies, plugins, build settings)
 
-------------------------------------------------------------------------------------

## 2. Prerequisites

Make sure you have:

- **JDK 17+** installed (and set as your project SDK)
- **Maven 3.8+** installed 
- An IDE such as **IntelliJ IDEA** or **Eclipse** with JavaFX support

------------------------------------------------------------------------------------

## 3. How to Build and Run

### Open Using IntelliJ IDEA (recommended)

1. **Clone the repository**

   ```bash
   git clone https://github.com/nathanvance/SFWE403-CLMS
   cd SFWE403-CLMS
   
2. **Open the project in IntelliJ**

- File → Open…
- Select the project root (the folder containing pom.xml).
- Let IntelliJ import Maven/Gradle dependencies
(It may take a minute for all JavaFX and JSON libraries to download.)
-Set the main class
- Locate App.java (or Main.java) in src/main/java.
- Right-click → Run 'App.main()' (or Run 'Main.main()').
- The JavaFX application window should open with the login screen as the starting view.
  
