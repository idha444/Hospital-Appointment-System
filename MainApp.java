import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The main class for the Hospital Appointment System.
 * This class creates the main frame and handles user login.
 */
public class MainApp extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private int loggedInUserId = -1;
    private String userRole = "";

    public MainApp() {
        setTitle("Hospital Appointment System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Add the login panel
        mainPanel.add(createLoginPanel(), "Login");

        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        // Action listener for the login button
        loginButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            authenticateUser(username, password);
        });

        return panel;
    }

    private void authenticateUser(String username, String password) {
        String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("id");
                userRole = rs.getString("role");
                
                JOptionPane.showMessageDialog(this, "Login Successful! Role: " + userRole.toUpperCase());
                
                // Switch to the appropriate dashboard
                switchToDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during login.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchToDashboard() {
        JPanel dashboard;
        switch (userRole) {
            case "admin":
                dashboard = new AdminDashboard(this);
                break;
            case "doctor":
                dashboard = new DoctorDashboard(loggedInUserId, this);
                break;
            case "patient":
                dashboard = new PatientDashboard(loggedInUserId, this);
                break;
            default:
                // Fallback to login screen
                cardLayout.show(mainPanel, "Login");
                return;
        }
        mainPanel.add(dashboard, "Dashboard");
        cardLayout.show(mainPanel, "Dashboard");
    }

    public void logout() {
        loggedInUserId = -1;
        userRole = "";
        // Remove the dashboard panel to ensure a fresh one is created on next login
        for(Component c : mainPanel.getComponents()) {
            if (!(c instanceof JPanel && ((JPanel)c).getLayout() instanceof GridBagLayout)) { // A bit of a hack to identify non-login panel
                 mainPanel.remove(c);
            }
        }
        cardLayout.show(mainPanel, "Login");
        JOptionPane.showMessageDialog(this, "You have been logged out.");
    }


    public static void main(String[] args) {
        // Run the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}
