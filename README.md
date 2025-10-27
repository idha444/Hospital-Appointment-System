======= HEAD
Hospital Appointment Booking System

A simple desktop application for managing hospital appointments, built with Java Swing and MySQL. This was created as a mini-project to demonstrate core Java and database connectivity concepts.

Features

Three User Roles: Admin, Doctor, and Patient with separate dashboards and functionalities.

Admin Dashboard:

Manage doctors (Add, Update, Delete).

Manage patients (Add, Update, Delete).

View all appointments in the system.

Patient Dashboard:

View available doctors and their specializations.

Book new appointments for a specific date and time.

View and cancel their own upcoming appointments.

Doctor Dashboard:

View their daily schedule of appointments.

Mark appointments as "Completed".

Technologies Used

Frontend: Java Swing

Backend: Java (JDBC)

Database: MySQL

Setup and Installation

To run this project locally, follow these steps:

Prerequisites:

Java Development Kit (JDK) 8 or newer.

MySQL Server.

MySQL Connector/J (JDBC Driver).

Database Setup:

Run the provided database_setup.sql script in your MySQL server to create the hospital_db database and tables.

Update the database credentials (DB_USER and DB_PASSWORD) in the DatabaseConnection.java file.

Get the JDBC Driver:

Download the MySQL Connector/J from the official MySQL website.

Place the .jar file (e.g., mysql-connector-j-9.4.0.jar) in the root of the project folder.

Compile and Run:

Open a terminal or command prompt in the project's root directory.

Compile all Java files:

javac -cp ".;mysql-connector-j-9.4.0.jar" *.java


Run the application:

java -cp ".;mysql-connector-j-9.4.0.jar" MainApp


Default Login Credentials

Admin: admin / adminpass

Doctor: d_adams / doctorpass

Patient: p_clark / patientpass
=======
# Hospital-Appointment-System
A Java Swing and MySQL app for booking hospital appointments.
======= 1364340be125e9ea3407936ab5cad1b9158183b6
