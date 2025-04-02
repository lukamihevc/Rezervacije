package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import controller.MainPageController;

public class MainPage {
    private JFrame frame;
    private JPanel stadiumPanel;

    public MainPage() {
        frame = new JFrame("Glavna stran");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Dodaj naslov
        JLabel titleLabel = new JLabel("Seznam igrišč", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(titleLabel, BorderLayout.NORTH);

        // Plošča za prikaz igrišč
        stadiumPanel = new JPanel();
        stadiumPanel.setLayout(new BoxLayout(stadiumPanel, BoxLayout.Y_AXIS));

        // Pridobivanje seznam igrišč in jih dodaj v panel
        MainPageController controller = new MainPageController();
        List<String> stadiums = controller.getStadiums();
        for (String stadium : stadiums) {
            JButton stadiumButton = new JButton(stadium);
            stadiumButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            stadiumPanel.add(stadiumButton);
        }

        // Dodaj panel za igrišča v glavno okno
        JScrollPane scrollPane = new JScrollPane(stadiumPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Nastavitve okna
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
