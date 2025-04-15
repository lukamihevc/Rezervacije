package view;
import db.DatabaseManager;
import controller.LoginController;
import model.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        frame = new JFrame("Prijava");
        frame.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Uporabniško ime:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Geslo:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Prijava");
        JButton registerButton = new JButton("Registracija");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                LoginController controller = new LoginController();

                // Preverimo, če je prijava uspešna
                if (controller.login(username, password)) {
                    // Preverimo, ali je uporabnik admin
                    DatabaseManager dbManager = new DatabaseManager();  // Tukaj uporabljamo DatabaseManager
                    int userId = dbManager.getUserIdByEmail(username);// Vrni ID uporabnika iz baze
                    Session.setCurrentUserId(userId);  // Nastavi ID uporabnika v Session
                    if (dbManager.isAdmin(username)) {  // Kličemo isAdmin iz DatabaseManager
                        JOptionPane.showMessageDialog(frame, "Prijava kot admin uspešna");
                        new AdminPanel(); // Odpri admin panel
                    } else {
                        JOptionPane.showMessageDialog(frame, "Prijava uspešna");
                        new MainPage(); // Odpri glavno stran za uporabnika
                    }
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Napačno uporabniško ime ali geslo", "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });




        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistrationPage();
                frame.dispose();
            }
        });

        frame.add(usernameLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(registerButton);

        frame.setSize(420, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
