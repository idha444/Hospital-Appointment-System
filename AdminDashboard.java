import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminDashboard extends JPanel {

    private MainApp mainApp;
    private JTable doctorsTable;
    private DefaultTableModel doctorsTableModel;
    private JTable patientsTable;
    private DefaultTableModel patientsTableModel;
    private JTable appointmentsTable;
    private DefaultTableModel appointmentsTableModel;

    public AdminDashboard(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel with Welcome Message and Logout Button
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainApp.logout());
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main content using JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Doctors", createDoctorsPanel());
        tabbedPane.addTab("Manage Patients", createPatientsPanel());
        tabbedPane.addTab("View All Appointments", createAppointmentsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Form fields
        JTextField nameField = new JTextField();
        JTextField specField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Form to add/update a doctor
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Details"));
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Specialization:"));
        formPanel.add(specField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("New Password (optional):"));
        formPanel.add(passwordField);

        // Buttons
        JButton addButton = new JButton("Add Doctor");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        // Table to display doctors
        doctorsTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Specialization", "Username"}, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false; // Make table cells not editable
             }
        };
        doctorsTable = new JTable(doctorsTableModel);
        panel.add(new JScrollPane(doctorsTable), BorderLayout.SOUTH);
        
        // Populate form when a doctor is selected from the table
        doctorsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && doctorsTable.getSelectedRow() != -1) {
                int selectedRow = doctorsTable.getSelectedRow();
                nameField.setText(doctorsTableModel.getValueAt(selectedRow, 1).toString());
                specField.setText(doctorsTableModel.getValueAt(selectedRow, 2).toString());
                usernameField.setText(doctorsTableModel.getValueAt(selectedRow, 3).toString());
                passwordField.setText(""); // Clear password for security
            }
        });

        // Action Listeners for buttons
        addButton.addActionListener(e -> addDoctor(nameField, specField, usernameField, passwordField));
        updateButton.addActionListener(e -> updateDoctor(nameField, specField, usernameField, passwordField));
        deleteButton.addActionListener(e -> deleteDoctor());

        loadDoctors();
        return panel;
    }
    
    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Form fields
        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Form to add/update a patient
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("New Password (optional):"));
        formPanel.add(passwordField);
        
        // Buttons
        JButton addButton = new JButton("Add Patient");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        // Table to display patients
        patientsTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Username"}, 0){
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
        };
        patientsTable = new JTable(patientsTableModel);
        panel.add(new JScrollPane(patientsTable), BorderLayout.SOUTH);

        // Populate form when a patient is selected
        patientsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && patientsTable.getSelectedRow() != -1) {
                int selectedRow = patientsTable.getSelectedRow();
                nameField.setText(patientsTableModel.getValueAt(selectedRow, 1).toString());
                usernameField.setText(patientsTableModel.getValueAt(selectedRow, 2).toString());
                passwordField.setText("");
            }
        });
        
        // Action Listeners
        addButton.addActionListener(e -> addPatient(nameField, usernameField, passwordField));
        updateButton.addActionListener(e -> updatePatient(nameField, usernameField, passwordField));
        deleteButton.addActionListener(e -> deletePatient());

        loadPatients();
        return panel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        appointmentsTableModel = new DefaultTableModel(new String[]{"Appt ID", "Patient Name", "Doctor Name", "Date", "Time", "Status"}, 0);
        appointmentsTable = new JTable(appointmentsTableModel);
        panel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);
        loadAllAppointments();
        return panel;
    }
    
    // --- Doctor Management Methods ---
    
    private void addDoctor(JTextField nameField, JTextField specField, JTextField usernameField, JPasswordField passwordField) {
        String fullName = nameField.getText();
        String specialization = specField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (fullName.isEmpty() || specialization.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required to add a new doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertUserSQL = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, 'doctor')";
        String insertDoctorSQL = "INSERT INTO doctors (user_id, specialization) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmtUser = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, username);
            pstmtUser.setString(2, password);
            pstmtUser.setString(3, fullName);
            pstmtUser.executeUpdate();
            
            long userId;
            try (ResultSet generatedKeys = pstmtUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userId = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            PreparedStatement pstmtDoctor = conn.prepareStatement(insertDoctorSQL);
            pstmtDoctor.setLong(1, userId);
            pstmtDoctor.setString(2, specialization);
            pstmtDoctor.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Doctor added successfully!");
            loadDoctors();
        } catch (SQLException ex) {
            // ... (rest of the catch block is the same)
        } finally {
            // ... (rest of the finally block is the same)
        }
    }

    private void updateDoctor(JTextField nameField, JTextField specField, JTextField usernameField, JPasswordField passwordField) {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) doctorsTableModel.getValueAt(selectedRow, 0);
        String fullName = nameField.getText();
        String specialization = specField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        String updateUserSQL = "UPDATE users SET full_name = ?, username = ?" + (password.isEmpty() ? "" : ", password = ?") + " WHERE id = ?";
        String updateDoctorSQL = "UPDATE doctors SET specialization = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            PreparedStatement pstmtUser = conn.prepareStatement(updateUserSQL);
            pstmtUser.setString(1, fullName);
            pstmtUser.setString(2, username);
            if (!password.isEmpty()) {
                pstmtUser.setString(3, password);
                pstmtUser.setInt(4, userId);
            } else {
                pstmtUser.setInt(3, userId);
            }
            pstmtUser.executeUpdate();

            PreparedStatement pstmtDoctor = conn.prepareStatement(updateDoctorSQL);
            pstmtDoctor.setString(1, specialization);
            pstmtDoctor.setInt(2, userId);
            pstmtDoctor.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Doctor updated successfully!");
            loadDoctors();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating doctor.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int userId = (int) doctorsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this doctor? This cannot be undone.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor deleted successfully.");
            loadDoctors();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting doctor. They may have existing appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- Patient Management Methods ---

    private void addPatient(JTextField nameField, JTextField usernameField, JPasswordField passwordField) {
        String fullName = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required to add a new patient.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, 'patient')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient added successfully!");
            loadPatients();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding patient. Username might already exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePatient(JTextField nameField, JTextField usernameField, JPasswordField passwordField) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) patientsTableModel.getValueAt(selectedRow, 0);
        String fullName = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        String sql = "UPDATE users SET full_name = ?, username = ?" + (password.isEmpty() ? "" : ", password = ?") + " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            if (!password.isEmpty()) {
                pstmt.setString(3, password);
                pstmt.setInt(4, userId);
            } else {
                pstmt.setInt(3, userId);
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient updated successfully!");
            loadPatients();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating patient.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePatient() {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int userId = (int) patientsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient deleted successfully.");
            loadPatients();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting patient.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Data Loading Methods ---

    private void loadDoctors() {
        doctorsTableModel.setRowCount(0);
        String sql = "SELECT u.id, u.full_name, d.specialization, u.username FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.role = 'doctor'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctorsTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("specialization"),
                    rs.getString("username")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load doctors.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPatients() {
        if (patientsTableModel == null) return;
        patientsTableModel.setRowCount(0);
        String sql = "SELECT id, full_name, username FROM users WHERE role = 'patient'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patientsTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("username")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load patients.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllAppointments() {
        appointmentsTableModel.setRowCount(0);
        String sql = "SELECT a.id, p.full_name as patient_name, d.full_name as doctor_name, a.appointment_date, a.appointment_time, a.status " +
                     "FROM appointments a " +
                     "JOIN users p ON a.patient_id = p.id " +
                     "JOIN users d ON a.doctor_id = d.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                appointmentsTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getDate("appointment_date").toString(),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}