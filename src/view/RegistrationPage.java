package view;

import controller.RegistrationController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationPage {
    private JFrame frame;
    private JTextField imeField;
    private JTextField priimekField;
    private JTextField emailField;
    private JPasswordField gesloField;
    private JTextField telefonField;
    private JTextField naslovField;

    public RegistrationPage() {
        frame = new JFrame("Registracija");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 300);
        frame.setLayout(new GridLayout(8, 2));

        frame.add(new JLabel("Ime:"));
        imeField = new JTextField();
        frame.add(imeField);

        frame.add(new JLabel("Priimek:"));
        priimekField = new JTextField();
        frame.add(priimekField);

        frame.add(new JLabel("E-pošta:"));
        emailField = new JTextField();
        frame.add(emailField);

        frame.add(new JLabel("Geslo:"));
        gesloField = new JPasswordField();
        frame.add(gesloField);

        frame.add(new JLabel("Telefon:"));
        telefonField = new JTextField();
        frame.add(telefonField);

        frame.add(new JLabel("Naslov:"));
        naslovField = new JTextField();
        frame.add(naslovField);

        JButton registerButton = new JButton("Registracija");
        JButton cancelButton = new JButton("Prekliči");

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrationController controller = new RegistrationController();
                boolean success = controller.register(
                        imeField.getText(),
                        priimekField.getText(),
                        new String(gesloField.getPassword()),
                        emailField.getText(),
                        telefonField.getText(),
                        naslovField.getText()
                );
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Registracija uspešna!");
                    new LoginPage();
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Uporabnik s tem e-naslovom že obstaja!", "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginPage();
                frame.dispose();
            }
        });

        frame.add(registerButton);
        frame.add(cancelButton);

        frame.setVisible(true);
    }
}
