package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import db.DatabaseManager;
import model.Field;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;

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
        JPanel rezervacijePanel = new JPanel();
        rezervacijePanel.setLayout(new BorderLayout());
        rezervacijePanel.add(new JLabel("Rezervacije še niso implementirane."), BorderLayout.CENTER);

        // Add the "Rezervacije" tab to the tabbed pane
        tabbedPane.addTab("Rezervacije", rezervacijePanel);  // "Rezervacije" tab

        // Add the tabbed pane to the main window
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Make the window visible
        frame.setVisible(true);
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
