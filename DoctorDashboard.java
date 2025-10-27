import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorDashboard extends JPanel {

    private int doctorId;
    private MainApp mainApp;
    private JTable appointmentsTable;
    private DefaultTableModel appointmentsTableModel;

    public DoctorDashboard(int doctorId, MainApp mainApp) {
        this.doctorId = doctorId;
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Doctor Dashboard - Your Schedule", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainApp.logout());
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Appointments Table
        String[] columnNames = {"Appointment ID", "Patient Name", "Date", "Time", "Status"};
        appointmentsTableModel = new DefaultTableModel(columnNames, 0);
        appointmentsTable = new JTable(appointmentsTableModel);
        add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsTableModel.setRowCount(0); // Clear existing data
        String sql = "SELECT a.id, p.full_name, a.appointment_date, a.appointment_time, a.status " +
                     "FROM appointments a " +
                     "JOIN users p ON a.patient_id = p.id " +
                     "WHERE a.doctor_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                appointmentsTableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getDate("appointment_date").toString(),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load your appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
