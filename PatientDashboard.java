import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;

public class PatientDashboard extends JPanel {
    
    private int patientId;
    private MainApp mainApp;
    private JComboBox<DoctorItem> doctorComboBox;
    private JTable appointmentsTable;
    private DefaultTableModel appointmentsTableModel;
    private JTextField dateField;
    private JComboBox<String> timeComboBox;

    // Helper class to store doctor info in JComboBox
    private static class DoctorItem {
        int id;
        String name;
        String specialization;

        DoctorItem(int id, String name, String specialization) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
        }

        @Override
        public String toString() {
            return name + " (" + specialization + ")";
        }
    }

    public PatientDashboard(int patientId, MainApp mainApp) {
        this.patientId = patientId;
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Patient Dashboard", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainApp.logout());
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main content in a JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createBookingPanel(), createAppointmentsPanel());
        splitPane.setResizeWeight(0.4);
        add(splitPane, BorderLayout.CENTER);

        loadDoctors();
        loadMyAppointments();
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Book a New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Doctor selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Doctor:"), gbc);
        doctorComboBox = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(doctorComboBox, gbc);

        // Date selection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Select Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField(10);
        gbc.gridx = 1;
        panel.add(dateField, gbc);

        // Time selection
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Select Time:"), gbc);
        timeComboBox = new JComboBox<>(new String[]{"09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM", "04:00 PM"});
        gbc.gridx = 1;
        panel.add(timeComboBox, gbc);
        
        // Book button
        JButton bookButton = new JButton("Book Appointment");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(bookButton, gbc);

        bookButton.addActionListener(this::bookAppointment);

        return panel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("My Appointments"));
        
        appointmentsTableModel = new DefaultTableModel(new String[]{"Appt ID", "Doctor", "Specialization", "Date", "Time", "Status"}, 0);
        appointmentsTable = new JTable(appointmentsTableModel);
        panel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel Selected Appointment");
        cancelButton.addActionListener(this::cancelAppointment);
        panel.add(cancelButton, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDoctors() {
        String sql = "SELECT u.id, u.full_name, d.specialization FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.role = 'doctor'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while(rs.next()) {
                doctorComboBox.addItem(new DoctorItem(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("specialization")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void bookAppointment(ActionEvent e) {
        DoctorItem selectedDoctor = (DoctorItem) doctorComboBox.getSelectedItem();
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int doctorId = selectedDoctor.id;
        String date = dateField.getText();
        String time = (String) timeComboBox.getSelectedItem();

        if (date.isEmpty() || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, this.patientId);
            pstmt.setInt(2, doctorId);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, time);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            loadMyAppointments(); // Refresh the appointments list
        } catch (SQLException ex) {
            // Unique constraint violation for double booking
            if (ex.getErrorCode() == 1062) {
                 JOptionPane.showMessageDialog(this, "This slot is already booked. Please choose another time or date.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Could not book appointment. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadMyAppointments() {
        appointmentsTableModel.setRowCount(0);
        String sql = "SELECT a.id, u.full_name, d.specialization, a.appointment_date, a.appointment_time, a.status " +
                     "FROM appointments a " +
                     "JOIN users u ON a.doctor_id = u.id " +
                     "JOIN doctors d ON u.id = d.user_id " +
                     "WHERE a.patient_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                appointmentsTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("specialization"),
                    rs.getDate("appointment_date").toString(),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelAppointment(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE appointments SET status = 'cancelled' WHERE id = ? AND status = 'booked'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, appointmentId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                loadMyAppointments(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Could not cancel appointment. It may already be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error while cancelling appointment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
