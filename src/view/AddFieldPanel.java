package view;

import db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class AddFieldPanel extends JPanel {
    private JTextField fieldNameField;
    private JComboBox<String> sportComboBox;
    private JComboBox<String> locationComboBox;
    private JTextField capacityField;
    private JTextField imagePathField;  // To display selected image path
    private JButton selectImageButton;  // Button to trigger file selection dialog
    private JButton addButton;
    private JButton cancelButton;

    public AddFieldPanel() {
        setLayout(new GridLayout(7, 2));

        JLabel fieldNameLabel = new JLabel("Ime igrišča:");
        fieldNameField = new JTextField();

        JLabel sportLabel = new JLabel("Šport:");
        String[] sports = {"Nogomet", "Košarka", "Odbojka", "Tenis"};
        sportComboBox = new JComboBox<>(sports);

        JLabel locationLabel = new JLabel("Kraj:");
        locationComboBox = new JComboBox<>();  // We'll populate this with location data from the database

        JLabel capacityLabel = new JLabel("Kapaciteta:");
        capacityField = new JTextField();

        JLabel imageLabel = new JLabel("Izberi sliko:");
        imagePathField = new JTextField();
        imagePathField.setEditable(false);

        selectImageButton = new JButton("Izberi sliko");
        selectImageButton.addActionListener(e -> openFileChooser());

        // Add components to panel
        add(fieldNameLabel);
        add(fieldNameField);
        add(sportLabel);
        add(sportComboBox);
        add(locationLabel);
        add(locationComboBox);
        add(capacityLabel);
        add(capacityField);
        add(imageLabel);
        add(imagePathField);
        add(selectImageButton);

        // Add the "Dodaj" and "Prekliči" buttons
        addButton = new JButton("Dodaj");
        cancelButton = new JButton("Prekliči");

        addButton.addActionListener(e -> addField());
        cancelButton.addActionListener(e -> cancel());

        // Add the buttons at the bottom of the panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel);

        // Populate locationComboBox with locations (you will implement getLocations() method)
        populateLocationComboBox();
    }

    // Open file chooser for selecting an image
    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Izberi sliko za igrišče");

        // Set filter for image files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg", "gif"));

        // Show file chooser dialog
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePathField.setText(selectedFile.getAbsolutePath());  // Display selected file path
        }
    }

    // Populate locationComboBox with locations from the database
    private void populateLocationComboBox() {
        DatabaseManager dbManager = new DatabaseManager();
        // Fetch locations from database and add to comboBox
        List<String> locations = dbManager.getKraji();
        for (String location : locations) {
            locationComboBox.addItem(location);
        }
    }

    // Method for handling the "Dodaj" button click
    private void addField() {
        String name = fieldNameField.getText();
        String sport = (String) sportComboBox.getSelectedItem();
        String location = (String) locationComboBox.getSelectedItem();
        String capacity = capacityField.getText();
        String imagePath = imagePathField.getText();

        if (name.isEmpty() || sport.isEmpty() || location.isEmpty() || capacity.isEmpty() || imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vse vrednosti so obvezne.", "Napaka", JOptionPane.ERROR_MESSAGE);
        } else {
            // Fetch location ID from the location name
            DatabaseManager dbManager = new DatabaseManager();
            int locationId = dbManager.getLocationId(location);

            // Try to add the field to the database
            boolean success = dbManager.addField(name, sport, imagePath, locationId, capacity);

            if (success) {
                JOptionPane.showMessageDialog(this, "Igrišče je bilo dodano!", "Uspešno", JOptionPane.INFORMATION_MESSAGE);
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                topFrame.dispose(); // Close the form window
            } else {
                JOptionPane.showMessageDialog(this, "Napaka pri dodajanju igrišča.", "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method for handling the "Prekliči" button click
    private void cancel() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // Close the form window
    }
}
