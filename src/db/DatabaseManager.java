package db;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://pg-31c972d-rezervacije.c.aivencloud.com:16281/defaultdb?ssl=require";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_RHD2IqWZglPPW-g2Wwv";
    private Connection connection; // Deklaracija spremenljivke connection

    // Konstruktor za povezovanje z bazo
    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace(); // Za boljšo diagnostiko
        }
    }

    static {
        try {
            // Poskrbi, da je PostgreSQL gonilnik registriran
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1); // Prekini aplikacijo, če gonilnik ni na voljo
        }
    }

    // Getter za povezavo z bazo
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Preverjanje, ali uporabnik obstaja in ali je geslo pravilno (geslo je hashirano)
    public boolean validateUser(String email, String password) {
        String query = "SELECT geslo FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("geslo");
                // Preverjanje hashiranega gesla
                return new BCryptPasswordEncoder().matches(password, hashedPassword);  // BCrypt je metoda za preverjanje hashiranega gesla
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Če uporabnik ne obstaja ali geslo ni pravilno
    }

    // Preverjanje, ali je uporabnik admin
    public boolean isAdmin(String email) {
        String query = "SELECT admin FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("admin"); // Če je v bazi stolpec "admin" nastavljen na true, uporabnik je admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Registracija uporabnika
    public boolean registerUser(String firstName, String lastName, String password, String email, String phone, String address) {
        // Preveri, ali uporabnik že obstaja
        if (userExists(email)) {
            return false; // Ne registriraj, če že obstaja uporabnik z istim e-naslovom
        }

        String query = "INSERT INTO users (ime, priimek, geslo, email, telefonska, naslov, admin) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Hashiraj geslo
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, hashedPassword); // Shrani hashirano geslo
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, address);
            stmt.setBoolean(7, false); // Privzeto ni admin
            return stmt.executeUpdate() > 0; // Vrne true, če je bila registracija uspešna
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Preveri, ali uporabnik že obstaja
    private boolean userExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Če obstaja vsaj ena vrstica, uporabnik že obstaja
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Testna povezava
    public void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Povezava je uspešna!");
            }
        } catch (SQLException e) {
            System.out.println("Napaka pri povezovanju z bazo: " + e.getMessage());
        }
    }
}
