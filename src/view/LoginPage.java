package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import controller.LoginController;

public class LoginPage {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        frame = new JFrame("Prijava");
        frame.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("E-pošta:");
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
                if (controller.login(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Prijava uspešna!");
                    // Tukaj lahko nadaljujete s prehodom na naslednjo stran
                } else {
                    JOptionPane.showMessageDialog(frame, "Napačen e-poštni naslov ali geslo.");
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistrationPage(); // Odpremo stran za registracijo
                frame.setVisible(false);
            }
        });

        frame.add(usernameLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(loginButton);
        frame.add(registerButton);

        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
