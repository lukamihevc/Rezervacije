package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import db.DatabaseManager;
import model.Field;
import model.Session;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;



public class MainPage {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private DatabaseManager dbManager;


    public MainPage() {
        dbManager = new DatabaseManager();

        frame = new JFrame("Glavna stran");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add a tabbed pane to hold both "Igriska" and "Rezervacije"
        tabbedPane = new JTabbedPane();

        // Create the "Igriska" tab and its content
        JPanel igriskaPanel = new JPanel();
        igriskaPanel.setLayout(new BorderLayout());

        // Create a table for the fields and set it up
        JTable fieldsTable = new JTable();
        JScrollPane fieldsScrollPane = new JScrollPane(fieldsTable);
        igriskaPanel.add(fieldsScrollPane, BorderLayout.CENTER);

        // Refresh the fields table with data from the database
        refreshFields(fieldsTable);  // Calls the method to populate the table

        // Add the "Igriska" tab to the tabbed pane
        tabbedPane.addTab("Igrisca", igriskaPanel);  // "Igriska" tab

        // Create the "Rezervacije" tab and its content (no functionality for now)

        // Zavihek REZERVACIJE
        JPanel rezervacijePanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Izberi datum (yyyy-MM-dd):"));
        JTextField datumField = new JTextField(10);
        topPanel.add(datumField);

        JButton osveziButton = new JButton("Osveži");
        topPanel.add(osveziButton);

        rezervacijePanel.add(topPanel, BorderLayout.NORTH);

// Tabela z igrišči
        String[] stolpci = {"Ime", "Lokacija", "Kapaciteta", "Status"};
        Object[][] data = new Object[0][4];  // za začetek prazno
        DefaultTableModel rezervacijeModel = new DefaultTableModel(data, stolpci) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable rezervacijeTable = new JTable(rezervacijeModel);
        rezervacijePanel.add(new JScrollPane(rezervacijeTable), BorderLayout.CENTER);

// Gumb za rezervacijo
        JButton rezervirajButton = new JButton("Rezerviraj izbrano");
        rezervacijePanel.add(rezervirajButton, BorderLayout.SOUTH);

// Dodaj zavihek
        tabbedPane.addTab("Rezervacije", rezervacijePanel);

        // TOP BAR Z GUMBOM ODJAVA
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton logoutButton = new JButton("Odjava");
        topBar.add(logoutButton);

        logoutButton.addActionListener(e -> logout());

        frame.add(topBar, BorderLayout.NORTH);  // Dodamo top bar zgoraj


// Add the tabbed pane to the main window
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);

// NOVO: mapa vrstica -> ID igrišča
        Map<Integer, Integer> vrsticaIdMap = new HashMap<>();

        osveziButton.addActionListener(e -> {
            String datum = datumField.getText().trim();
            if (datum.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Prosim vnesi datum.");
                return;
            }

            List<Field> fields = dbManager.getAllFields();
            rezervacijeModel.setRowCount(0);  // počisti tabelo
            vrsticaIdMap.clear(); // počistimo mapo

            int vrsticaIndex = 0;

            for (Field field : fields) {
                try {
                    LocalDate localDate = LocalDate.parse(datum);
                    Timestamp zacetek = Timestamp.valueOf(localDate.atStartOfDay());
                    Timestamp konec = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay());

                    boolean prosto = dbManager.jeIgrisceProsto(field.getId(), zacetek, konec);
                    String status = prosto ? "PROSTO" : "ZASEDENO";

                    rezervacijeModel.addRow(new Object[]{
                            field.getIme(),
                            dbManager.getKrajNameById(field.getKrajId()),
                            field.getKapaciteta(),
                            status
                    });

                    vrsticaIdMap.put(vrsticaIndex, field.getId());
                    vrsticaIndex++;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Napaka pri preverjanju terminov.");
                }
            }

            // Barvanje vrstic
            rezervacijeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String status = (String) rezervacijeModel.getValueAt(row, 3);
                    if ("PROSTO".equals(status)) {
                        c.setBackground(Color.GREEN);
                    } else {
                        c.setBackground(Color.PINK);
                    }
                    return c;
                }
            });
        });

        rezervirajButton.addActionListener(e -> {
            int row = rezervacijeTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Izberi igrišče za rezervacijo.");
                return;
            }

            String status = (String) rezervacijeModel.getValueAt(row, 3);
            if (!"PROSTO".equals(status)) {
                JOptionPane.showMessageDialog(frame, "To igrišče je zasedeno.");
                return;
            }

            // Dobimo ID iz mape
            int igrisceId = vrsticaIdMap.get(row);
            String datum = datumField.getText().trim();

            try {
                // Pretvori v LocalDate
                LocalDate localDate = LocalDate.parse(datum);

                // Začetek rezervacije - ob 00:00
                Timestamp zacetek = Timestamp.valueOf(localDate.atStartOfDay());

                // Konec rezervacije - naslednji dan ob 00:00
                Timestamp konec = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay());

                // Uporabniški ID - tukaj uporabi ID prijavljenega uporabnika, trenutno je hardcoded na 1
                int userId = 1;  // <-- začasno hardcoded user (če imaš prijavo, to spremeni)

                // Kliči funkcijo za rezervacijo
                boolean uspeh = dbManager.rezervirajIgrisce(igrisceId, zacetek, konec);

                if (uspeh) {
                    JOptionPane.showMessageDialog(frame, "Uspešno rezervirano!");
                    osveziButton.doClick();  // osveži tabelo
                } else {
                    JOptionPane.showMessageDialog(frame, "Rezervacija ni uspela.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Napaka pri rezervaciji.");
            }
        });

    }

    private void logout() {
        Session.logout();
        frame.dispose();
        new LoginPage();
    }





    private void refreshFields(JTable fieldsTable) {
        // Fetch fields data
        List<Field> fields = dbManager.getAllFields();
        String[] columns = {"Ime igrišča", "Lokacija", "Kapaciteta", "Slika"};
        Object[][] data = new Object[fields.size()][4];

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            data[i][0] = field.getIme();
            data[i][1] = dbManager.getKrajNameById(field.getKrajId());  // Assume you have this method
            data[i][2] = field.getKapaciteta();
            data[i][3] = getImageIcon(dbManager.getFieldImagePath(field.getId()));  // Assuming this is a valid method
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make the entire table non-editable
            }
        };
        fieldsTable.setModel(model);
        fieldsTable.getColumnModel().getColumn(3).setCellRenderer(new ImageRenderer());  // Set image renderer
        fieldsTable.setRowHeight(60);  // Adjust row height for better image display
    }

    // Method to get ImageIcon for a field (resizing image to fit in the cell)
    private ImageIcon getImageIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage(); // Transform the ImageIcon into an Image
        Image resizedImage = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);  // Resize to fit the cell
        return new ImageIcon(resizedImage);  // Return resized ImageIcon
    }

    // Custom ImageRenderer for table cell rendering (center the image in the cell)
    private class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof ImageIcon) {
                label.setIcon((ImageIcon) value);
                label.setText("");  // Ensure no text is displayed
                label.setHorizontalAlignment(SwingConstants.CENTER);  // Center the image horizontally
                label.setVerticalAlignment(SwingConstants.CENTER);    // Center the image vertically
            }
            return label;
        }
    }
}
