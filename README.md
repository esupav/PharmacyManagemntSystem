# 🏥 Pharmacy Management System

A professional, secure, and regulation-compliant desktop application designed for modern pharmacies. Built with **JavaFX**, **MySQL**, and **Maven**, this system streamlines inventory management, sales, and reporting while strictly adhering to health organization standards.


## 🚀 Key Features

### 🔐 Security & Access Control
*   **Role-Based Access:** Strict separation between **Admin** (Management) and **Pharmacist** (Sales) roles.
*   **Secure Login:** Encrypted authentication system.
*   **Audit Logging:** Every critical action (login, sale, inventory change) is recorded for accountability.

### 💊 Inventory Management (Admin)
*   **Full CRUD:** Add, Update, Delete, and View medicines.
*   **Stock Tracking:** Real-time quantity updates.
*   **Expiry Alerts:** Visual warnings for medicines nearing expiration.
*   **Supplier Management:** Maintain a database of licensed suppliers.

### 🛒 Point of Sale (POS) (Pharmacist)
*   **Efficient Sales:** Fast and user-friendly interface for processing transactions.
*   **Prescription Validation:** **Mandatory check** for doctor's name on Rx-only drugs.
*   **Expiry Protection:** System **blocks sales** of expired medicines automatically.
*   **Live Drug Info:** Integrated **OpenFDA API** fetches real-time drug descriptions and usage info.
*   **Payment Modes:** Supports **Cash** and **Credit** sales.

### 📊 Reporting & Analytics
*   **Sales Reports:** Detailed history of every transaction.
*   **Performance Tracking:** Track sales by pharmacist to monitor staff performance.
*   **Daily Reports:** Pharmacists can view their own daily sales totals.
*   **Visual Charts:** Interactive charts for sales trends and revenue analysis.



## 🛠️ Technology Stack

*   **Language:** Java 21
*   **UI Framework:** JavaFX 21 (Modular)
*   **Database:** MySQL 8.0
*   **Build Tool:** Maven
*   **Icons:** Ikonli (FontAwesome 5)
*   **API:** OpenFDA (for drug information)


## ⚙️ Installation & Setup

### Prerequisites
1.  **Java JDK 21** or higher installed.
2.  **MySQL Server** installed and running.
3.  **Maven** installed (or use your IDE's built-in Maven).

### Database Setup
1.  Open your MySQL client (Workbench, CLI, etc.).
2.  Create the database:
    sql
    CREATE DATABASE pharmacy_db;

3.  The application will **automatically create all necessary tables** and the default admin account upon the first run.

### Running the Application
1.  Clone this repository.
2.  Open the project in **IntelliJ IDEA** (recommended).
3.  Update the database credentials in `src/main/java/com/pharmacy/database/DBconnection.java`:
    java
    private static final String USER = "root";
    private static final String PASSWORD = "@Etoma1996";

4.  Run the `HelloApplication.java` file.



## 👤 User Roles & Default Credentials

### **Admin**
*   **Username:** `admin`
*   **Password:** `admin123`
*   **Capabilities:** Full system control, inventory management, user creation, reporting.

### **Pharmacist**
*   **Username:** (Create a new one via Admin dashboard)
*   **Default Test User:** `pharmacist` / `user123` (if seeded)
*   **Capabilities:** Sales (POS), Customer registration, Daily report view.



## 📜 License
This project is developed for educational and professional portfolio purposes.



## 🤝 Contributing
Contributions are welcome! Please fork the repository and submit a pull request.
