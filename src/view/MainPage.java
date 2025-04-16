package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.List;
import db.DatabaseManager;
import model.Field;
import model.Session;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import org.jdatepicker.impl.DateComponentFormatter;



public class MainPage {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private DatabaseManager dbManager;
    private JTable mojeRezervacijeTable;
    private Map<Integer, Integer> vrsticaIdMap;


    public MainPage() {
        dbManager = new DatabaseManager();

        frame = new JFrame("Glavna stran");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Glavni tabbed pane
        tabbedPane = new JTabbedPane();

        // ---------- TAB: IGRISCA ----------
        JPanel igriskaPanel = new JPanel(new BorderLayout());
        JTable fieldsTable = new JTable();
        igriskaPanel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);
        refreshFields(fieldsTable);
        tabbedPane.addTab("Igriska", igriskaPanel);

        // ---------- TAB: MOJE REZERVACIJE ----------
        JPanel panelRezervacije = new JPanel(new BorderLayout());
        prikaziMojeRezervacije(panelRezervacije);

        // Gumbi za razveljavitev in spremembo datuma
        JButton razveljaviButton = new JButton("Razveljavi rezervacijo");
        JButton spremeniDatumButton = new JButton("Spremeni datum");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(razveljaviButton);
        bottomPanel.add(spremeniDatumButton);
        panelRezervacije.add(bottomPanel, BorderLayout.SOUTH);  // Add the buttons here

        tabbedPane.addTab("Moje rezervacije", panelRezervacije);

        // ---------- TAB: REZERVACIJE ----------
        JPanel rezervacijePanel = new JPanel(new BorderLayout());

        // Zgornji panel z izbirnikom datuma in gumbom za osvežitev
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Izberi datum"));
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Danes");
        p.put("text.month", "Mesec");
        p.put("text.year", "Leto");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        topPanel.add(datePicker);
        JButton osveziButton = new JButton("Osveži");
        topPanel.add(osveziButton);
        rezervacijePanel.add(topPanel, BorderLayout.NORTH);

        // Tabela z igrišči in njihovim statusom
        String[] stolpci = {"Ime", "Lokacija", "Kapaciteta", "Status"};
        DefaultTableModel rezervacijeModel = new DefaultTableModel(stolpci, 0) {
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

        tabbedPane.addTab("Rezervacije", rezervacijePanel);

        // ---------- GORNA ORODNO VRSTICO ----------
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton logoutButton = new JButton("Odjava");
        logoutButton.addActionListener(e -> logout());
        topBar.add(logoutButton);

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // Mapa vrstica -> ID igrišča
        vrsticaIdMap = new HashMap<>();

        // OSVEŽI funkcionalnost
        osveziButton.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(frame, "Prosim izberi datum.");
                return;
            }

            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            List<Field> fields = dbManager.getAllFields();
            rezervacijeModel.setRowCount(0);
            vrsticaIdMap.clear();

            int vrsticaIndex = 0;
            for (Field field : fields) {
                try {
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

        // REZERVIRAJ funkcionalnost
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

            int igrisceId = vrsticaIdMap.get(row);
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(frame, "Prosim izberi datum.");
                return;
            }

            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Timestamp zacetek = Timestamp.valueOf(localDate.atStartOfDay());
            Timestamp konec = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay());
            int userId = Session.getCurrentUserId();

            try {
                boolean uspeh = dbManager.rezervirajIgrisce(igrisceId, zacetek, konec);
                if (uspeh) {
                    JOptionPane.showMessageDialog(frame, "Uspešno rezervirano!");
                    osveziButton.doClick();
                } else {
                    JOptionPane.showMessageDialog(frame, "Rezervacija ni uspela.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Napaka pri rezervaciji.");
            }
        });

        // RAZVELJAVI funkcionalnost
        razveljaviButton.addActionListener(e -> {
            int row = mojeRezervacijeTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Izberi rezervacijo za razveljavitev.");
                return;
            }

            // Preberi ID rezervacije iz tabele

        });

        // SPREMENI datum funkcionalnost
        spremeniDatumButton.addActionListener(e -> {
            int row = mojeRezervacijeTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Izberi rezervacijo za spremembo datuma.");
                return;
            }

            // Pridobi ID rezervacije
            int rezervacijaId = (int) mojeRezervacijeTable.getValueAt(row, 0);
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(frame, "Prosim izberi datum.");
                return;
            }

            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Timestamp zacetek = Timestamp.valueOf(localDate.atStartOfDay());
            Timestamp konec = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay());

            try {
                boolean uspeh = dbManager.spremeniDatumRezervacije(rezervacijaId, zacetek, konec);
                if (uspeh) {
                    JOptionPane.showMessageDialog(frame, "Datum rezervacije spremenjen!");
                    prikaziMojeRezervacije(panelRezervacije);
                } else {
                    JOptionPane.showMessageDialog(frame, "Napaka pri spreminjanju datuma.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Napaka pri spreminjanju datuma.");
            }
        });
    }



    private void prikaziMojeRezervacije(JPanel panel) {
        int userId = Session.getCurrentUserId();

        // Preverimo, ali je uporabnik prijavljen
        if (userId == -1) {
            JOptionPane.showMessageDialog(frame, "Uporabnik ni prijavljen!");
            return;
        }

        // Tabela s stolpci
        String[] stolpci = {"Igrišče", "Lokacija", "Začetek", "Konec"};
        DefaultTableModel model = new DefaultTableModel(stolpci, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabela = new JTable(model);

        try {
            // Pridobi rezervacije iz baze
            List<Reservation> rezervacije = dbManager.getRezervacijeUporabnika(userId);

            if (rezervacije.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ni nobenih rezervacij za prikaz.");
            }

            for (Reservation r : rezervacije) {
                // Pravilno pridobimo ime igrišča glede na ID
                String imeIgrisce = dbManager.getImeIgriscaById(r.getIgrisceId());

                // Pravilno pridobimo lokacijo preko kraja povezanega z igriščem
                int krajId = dbManager.getKrajIdByIgrisceId(r.getIgrisceId());
                String lokacija = dbManager.getKrajNameById(krajId);

                // Obrazec za prikaz datuma
                String zacetek = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getZacetek());
                String konec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getKonec());

                model.addRow(new Object[]{
                        imeIgrisce,
                        lokacija,
                        zacetek,
                        konec
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Napaka pri pridobivanju rezervacij.");
        }

        // Osveži prikaz tabele
        panel.removeAll();
        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
        mojeRezervacijeTable = tabela;
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
