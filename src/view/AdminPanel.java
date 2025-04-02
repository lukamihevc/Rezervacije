package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminPanel {
    public AdminPanel() {
        JFrame frame = new JFrame("Admin Panel");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Ustvarimo JTabbedPane za dva zavihka
        JTabbedPane tabbedPane = new JTabbedPane();

        // Zavihek za upravljanje z igrišči
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BorderLayout());

        // Dodamo tabelo za igrišča
        String[] fieldColumns = {"ID", "Ime igrišča", "Lokacija", "Kapaciteta"};
        Object[][] fieldData = {
                {"1", "Igrišče A", "Ljubljana", "10"},
                {"2", "Igrišče B", "Maribor", "8"},
                {"3", "Igrišče C", "Celje", "6"}
        };

        JTable fieldsTable = new JTable(fieldData, fieldColumns);
        JScrollPane fieldsScrollPane = new JScrollPane(fieldsTable);
        fieldsPanel.add(fieldsScrollPane, BorderLayout.CENTER);

        // Zavihek za upravljanje z uporabniki
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BorderLayout());

        // Dodamo tabelo za uporabnike
        String[] userColumns = {"ID", "Ime", "Priimek", "Email", "Telefonska"};
        Object[][] userData = {
                {"1", "Janez", "Novak", "janez@example.com", "031123456"},
                {"2", "Maja", "Kovač", "maja@example.com", "041654321"},
                {"3", "Luka", "Mihevc", "luka@example.com", "070629206"}
        };

        JTable usersTable = new JTable(userData, userColumns);
        JScrollPane usersScrollPane = new JScrollPane(usersTable);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);

        // Dodamo zavihke v tabbedPane
        tabbedPane.addTab("Igrišča", fieldsPanel);
        tabbedPane.addTab("Uporabniki", usersPanel);

        // Dodamo tabbedPane v glavni okvir
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Nastavimo okno
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
