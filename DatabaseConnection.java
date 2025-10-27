import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles the connection to the MySQL database.
 * This class provides a static method to get a database connection.
 */
public class DatabaseConnection {
    // --- IMPORTANT ---
    // Update these values to match your MySQL database configuration.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String DB_USER = "root"; // Your MySQL username
    private static final String DB_PASSWORD = "priya46"; // Your MySQL password

    /**
     * Establishes and returns a connection to the database.
     * @return A Connection object or null if the connection fails.
     */
    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the connection
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            // Print error details if connection fails
            e.printStackTrace();
            System.err.println("Database connection failed. Check your credentials and MySQL server status.");
            return null;
        }
    }
}
