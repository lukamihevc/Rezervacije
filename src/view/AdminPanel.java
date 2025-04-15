package view;

import model.Field;
import model.Session;
import model.User;
import db.DatabaseManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.UserTableModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminPanel {
    private JTable fieldsTable;
    private JTable usersTable;
    private DatabaseManager dbManager;

    public AdminPanel() {
        dbManager = new DatabaseManager();

        JFrame frame = new JFrame("Admin Panel");
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel fieldsPanel = createFieldsPanel();
        JPanel usersPanel = createUsersPanel(tabbedPane);


        tabbedPane.addTab("Igrišča", fieldsPanel);
        tabbedPane.addTab("Uporabniki", usersPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        fieldsTable = new JTable();
        refreshFields();
        JScrollPane scrollPane = new JScrollPane(fieldsTable);

        JButton addButton = new JButton("Dodaj igrišče");
        addButton.addActionListener(e -> openAddFieldPanel());

        JButton refreshButton = new JButton("Osveži");
        refreshButton.addActionListener(e -> refreshFields());

        JButton deleteButton = new JButton("Briši");
        deleteButton.addActionListener(e -> deleteField());

        JButton logoutButton = new JButton("Odjava");
        logoutButton.addActionListener(e -> logout());


        JPanel controlPanel = new JPanel();
        controlPanel.add(addButton);
        controlPanel.add(refreshButton);
        controlPanel.add(deleteButton);
        controlPanel.add(logoutButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUsersPanel(JTabbedPane tabbedPane) {
        JPanel usersPanel = new JPanel(new BorderLayout());

        if (usersTable == null) {
            usersTable = new JTable();  // Initialize JTable if it's null
            usersTable.setModel(new DefaultTableModel()); // Optionally, set an initial empty model
        }

        // Pripravimo tabelo uporabnikov
        refreshUsers();
        JScrollPane usersScrollPane = new JScrollPane(usersTable);

        // Gumbi za upravljanje uporabnikov
        JButton addUserButton = new JButton("Dodaj uporabnika");
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        JButton deleteUserButton = new JButton("Briši uporabnika");
        deleteUserButton.addActionListener(e -> deleteUser());

        JButton refreshUsersButton = new JButton("Osveži");
        refreshUsersButton.addActionListener(e -> refreshUsers());

        // Ustvarite gumb za spreminjanje statusa admina
        JButton changeAdminStatusButton = new JButton("Spremeni status admina");
        changeAdminStatusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeAdminStatus();
            }
        });

        JButton logoutButton = new JButton("Odjava");
        logoutButton.addActionListener(e -> logout());
        // Panel za gumbe
        JPanel usersControlPanel = new JPanel();  // **Pomembno: deklarirajte tukaj**
        usersControlPanel.add(addUserButton);
        usersControlPanel.add(deleteUserButton);
        usersControlPanel.add(refreshUsersButton);
        usersControlPanel.add(changeAdminStatusButton); // Dodajte gumb za spremembo statusa admina
        usersControlPanel.add(logoutButton); // Dodaj gumb za odjavo


        // Dodamo elemente v usersPanel
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);
        usersPanel.add(usersControlPanel, BorderLayout.SOUTH);

        // Dodamo zavihek
        tabbedPane.addTab("Uporabniki", usersPanel);

        return usersPanel;
    }

    private void addUser() {
        // Tukaj morate zbirati podatke za novega uporabnika
        String ime = JOptionPane.showInputDialog("Vnesite ime uporabnika:");
        String priimek = JOptionPane.showInputDialog("Vnesite priimek uporabnika:");
        String email = JOptionPane.showInputDialog("Vnesite e-poštni naslov:");
        String geslo = JOptionPane.showInputDialog("Vnesite geslo:");
        String telefon = JOptionPane.showInputDialog("Vnesite telefonsko številko:");
        String naslov = JOptionPane.showInputDialog("Vnesite naslov uporabnika:");

        // Preverimo, ali so vsi podatki vneseni
        if (ime != null && priimek != null && email != null && geslo != null && telefon != null && naslov != null) {
            // Ustvarimo nov objekt User, zdaj z vsemi potrebnimi podatki
            User newUser = new User(0, ime, priimek, email, geslo, false, telefon, naslov); // 0 je začasen ID, ki se bo nastavil v bazi

            // Poskusimo dodati uporabnika v bazo, zdaj s telefonsko številko in naslovom
            boolean success = registerUser(newUser, telefon, naslov);  // Pass telefon and naslov as arguments

            if (success) {
                JOptionPane.showMessageDialog(null, "Uporabnik uspešno dodan!");
                refreshUsers(); // Osveži seznam uporabnikov po dodajanju
            } else {
                JOptionPane.showMessageDialog(null, "Napaka pri dodajanju uporabnika. Morda že obstaja.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Vsi podatki morajo biti vnešeni.");
        }
    }

    private void changeAdminStatus() {
        int selectedRow = usersTable.getSelectedRow(); // Poišči izbranega uporabnika v tabeli

        if (selectedRow >= 0) { // Če je uporabnik izbran
            User selectedUser = dbManager.getAllUsers().get(selectedRow); // Poberi podatke o uporabniku
            boolean currentAdminStatus = selectedUser.isAdmin(); // Preveri trenutni status admina

            // Obrni status admina (če je 0, nastavi na 1, in obratno)
            boolean newAdminStatus = !currentAdminStatus;

            // Posodobi status admina v bazi, zdaj posreduj samo dva argumenta
            boolean success = updateAdminStatus(selectedUser.getId(), newAdminStatus);

            if (success) {
                JOptionPane.showMessageDialog(null, "Status admina uspešno spremenjen!");
                refreshUsers(); // Osveži seznam uporabnikov po spremembi
            } else {
                JOptionPane.showMessageDialog(null, "Napaka pri spreminjanju statusa admina.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Izberite uporabnika za spremembo statusa.");
        }
    }

    private boolean updateAdminStatus(int userId, boolean newAdminStatus) {
        String query = "UPDATE users SET admin = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, newAdminStatus); // Nastavi nov status admina
            stmt.setInt(2, userId); // Nastavi ID uporabnika

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Če je bilo spremenjenih več kot 0 vrstic, je bila sprememba uspešna
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerUser(User newUser, String telefon, String naslov) {
        if (dbManager.userExists(newUser.getEmail())) {
            return false; // Ne registriraj, če že obstaja uporabnik z istim e-naslovom
        }

        String query = "INSERT INTO users (ime, priimek, geslo, email, telefonska, naslov, admin) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String hashedPassword = newUser.getGeslo(); // Tu bi morali uporabiti hashiranje (npr. BCrypt)

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newUser.getIme());
            stmt.setString(2, newUser.getPriimek());
            stmt.setString(3, hashedPassword); // Shrani hashirano geslo
            stmt.setString(4, newUser.getEmail());
            stmt.setString(5, telefon); // Uporabite tudi podatke iz forme
            stmt.setString(6, naslov);  // Uporabite tudi podatke iz forme
            stmt.setBoolean(7, newUser.isAdmin()); // Privzeto ni admin
            return stmt.executeUpdate() > 0; // Vrne true, če je bila registracija uspešna
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void refreshUsers() {
        // Get all users from the database
        List<User> users = dbManager.getAllUsers();

        // Set the table columns (Include columns for ID, name, surname, email, and admin status)
        String[] columns = {"Ime", "Priimek", "E-pošta", "Status Admina"};

        // Prepare data with the ID stored as Integer in the first column
        Object[][] data = new Object[users.size()][4];  // 4 columns (no ID column in the view)

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            data[i][0] = user.getIme();
            data[i][1] = user.getPriimek();
            data[i][2] = user.getEmail();
            data[i][3] = user.isAdmin() ? "Da" : "Ne";  // Display admin status as "Da" or "Ne"
        }

        // Set the table model
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make the entire table non-editable
            }
        };

        // Set the table model
        usersTable.setModel(model);

        // Add the user ID as an additional hidden column for internal processing
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            // Ensure ID is stored in a separate internal list or object for reference
            usersTable.getModel().setValueAt(user.getId(), i, 0);  // Setting the ID value in the first column
        }

        // Hide the first column (ID column)
        usersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        usersTable.getColumnModel().getColumn(0).setMinWidth(0);
        usersTable.getColumnModel().getColumn(0).setWidth(0);
    }

    private void openRegistrationPanel() {
        new RegistrationPage(); // Samo ustvari novo okno, če že podeduje JFrame
    }

    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();  // Get the selected row
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Izberite uporabnika za brisanje.");
            return;
        }

        // Retrieve the user ID from the hidden column (the first column, index 0)
        Object value = usersTable.getValueAt(selectedRow, 0);  // The ID is in the first column, but hidden

        // Debugging: Print the retrieved value
        System.out.println("Retrieved value: " + value);

        int userId = -1;  // Default value for invalid IDs
        try {
            // Try parsing the value as an integer
            userId = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            // Handle case where the value is not a valid integer
            JOptionPane.showMessageDialog(null, "Neveljavna ID številka uporabnika.");
            return;
        }

        // If user ID is -1 (invalid), show a message
        if (userId == -1) {
            JOptionPane.showMessageDialog(null, "Uporabnik ni bil najden.");
            return;
        }

        // Ask for confirmation before deleting the user
        int confirm = JOptionPane.showConfirmDialog(null, "Ste prepričani, da želite izbrisati uporabnika?", "Potrditev", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dbManager.deleteUser(userId);  // Call the method to delete the user from the database
            refreshUsers();  // Refresh the user list in the table
        }
    }

    private void refreshFields() {
        List<Field> fields = dbManager.getAllFields();
        String[] columns = {"Ime igrišča", "Lokacija", "Kapaciteta", "Slika"};
        Object[][] data = new Object[fields.size()][4];

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            data[i][0] = field.getIme();
            data[i][1] = dbManager.getKrajNameById(field.getKrajId());
            data[i][2] = field.getKapaciteta();
            data[i][3] = getImageIcon(dbManager.getFieldImagePath(field.getId()));
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fieldsTable.setModel(model);
        fieldsTable.getColumnModel().getColumn(3).setCellRenderer(new ImageRenderer());
        fieldsTable.setRowHeight(60);
    }

    private ImageIcon getImageIcon(String path) {
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        }
        return null;
    }

    private void openAddFieldPanel() {
        JFrame frame = new JFrame("Dodaj igrišče");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new AddFieldPanel());
        frame.setVisible(true);
    }

    private void deleteField() {
        int selectedRow = fieldsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Izberite igrišče za brisanje.");
            return;
        }
        Field selectedField = dbManager.getAllFields().get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(null, "Ste prepričani, da želite izbrisati igrišče?", "Potrditev", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dbManager.deleteField(selectedField.getId());
            refreshFields();
        }
    }

    static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                return new JLabel((ImageIcon) value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private void logout() {
        // Kličeš metodo logout v razredu Session, da odjaviš uporabnika
        Session.logout();

        // Zapreš trenutni AdminPanel okvir
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(fieldsTable); // Poišči nadrejeni okvir
        if (topFrame != null) {
            topFrame.dispose();  // Zapri trenutni okvir
        }

        // Odpri login stran
        new LoginPage();  // Predpostavljam, da imaš razred LoginPage, ki predstavlja prijavno stran
    }


}
