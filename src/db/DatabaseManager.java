package db;



import model.Session;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import model.Field;  // Uvoz razreda Field

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.User;
import view.Reservation;

import java.sql.Timestamp;

import static java.util.jar.Pack200.Packer.PASS;


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

    // Povezovanje z bazo
    public Connection connect() throws SQLException {
        try {
            // Vzpostavi povezavo z bazo
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Napaka pri povezovanju z bazo: " + e.getMessage());
            throw e;
        }
    }

    // Getter za povezavo z bazo
    public static Connection getConnection() throws SQLException {
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
    public boolean userExists(String email) {
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

    public List<String> getKraji() {
        List<String> krajiList = new ArrayList<>();
        String query = "SELECT ime FROM kraji"; // Adjust the query to fit your table structure
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                krajiList.add(rs.getString("ime")); // Add each location name to the list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return krajiList;
    }

    public boolean addField(String name, String sport, String imagePath, int locationId, String capacity) {
        // Updated query to match the column type of 'kapaciteta' as TEXT
        String query = "INSERT INTO igrisca (ime, sport, slika, kraj_id, kapaciteta) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the parameters for the query
            stmt.setString(1, name);  // Name of the field (ime)
            stmt.setString(2, sport);  // Sport type (sport)
            stmt.setString(3, imagePath);  // Image path (slika)
            stmt.setInt(4, locationId);  // Location ID (kraj_id)
            stmt.setString(5, capacity);  // Capacity (kapaciteta) as TEXT

            // Execute the insert query and check the result
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Return true if rows were inserted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Return false if there was an error
        }
    }


    public int getLocationId(String locationName) {
        String query = "SELECT id FROM kraji WHERE ime = ?";  // Assuming 'ime' is the location name column

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, locationName);  // Set the location name
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");  // Return the ID of the location
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;  // Return -1 if no location found (this could be handled with an error message)
    }

    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT id, ime, sport, slika, kraj_id, kapaciteta FROM igrisca";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String ime = rs.getString("ime");
                String sport = rs.getString("sport");
                String slika = rs.getString("slika");
                int krajId = rs.getInt("kraj_id");
                int kapaciteta = rs.getInt("kapaciteta");

                Field field = new Field(id, ime, sport, slika, krajId, kapaciteta);
                fields.add(field);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fields;
    }

    public boolean updateField(int fieldId, String name, int capacity, int locationId, String imagePath) {
        String updateQuery = "UPDATE igrisca SET ime = ?, kapaciteta = ?, kraj_id = ?, slika = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            // Nastavi parametre
            stmt.setString(1, name);
            stmt.setInt(2, capacity);
            stmt.setInt(3, locationId);  // Tukaj pričakujemo int (ID kraja)
            stmt.setString(4, imagePath);
            stmt.setInt(5, fieldId);  // Zagotovi, da je to ID, ki obstaja v bazi

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Če je bila posodobitev uspešna
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;  // Če je prišlo do napake pri izvajanju
    }


    public List<String> getAllLocationNames() {
        List<String> locations = new ArrayList<>();
        String query = "SELECT ime FROM kraji";  // Predpostavljamo, da imamo tabelo 'kraji' z imeni krajev

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                locations.add(rs.getString("ime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }



    public int getLocationIdByName(String locationName) {
        String query = "SELECT id FROM kraji WHERE ime = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, locationName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // V primeru, da ni bilo najdeno
    }


    // Metoda za brisanje igrišča
    public void deleteField(int id) {
        String query = "CALL delete_field(?)"; // Pravilno za procedure
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.execute(); // execute(), ne executeUpdate(), ker ne gre za UPDATE/DELETE/INSERT
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getKrajNameById(int krajId) {
        String query = "SELECT ime FROM kraji WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, krajId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Neznana lokacija"; // Če lokacija ni najdena
    }

    public String getFieldImagePath(int fieldId) {
        String imagePath = null;
        String query = "SELECT slika FROM igrisca WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, fieldId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                imagePath = rs.getString("slika");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return imagePath;
    }


    public boolean updateField(int id, String name, int capacity, String location, String imagePath) {
        // Popravljena SQL poizvedba
        String query = "UPDATE igrisca SET ime = ?, kapaciteta = ?, kraj_id = ?, image_path = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            // Nastavi parametre
            stmt.setString(1, name);
            stmt.setInt(2, capacity);
            stmt.setString(3, location); // Dodaj parametre za location
            stmt.setString(4, imagePath);
            stmt.setInt(5, id);

            // Izvedi poizvedbo
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public Icon getFieldImage(int fieldId) {
        // Predpostavljamo, da imamo pot do slike shranjeno v bazi, npr. kot besedilo
        String imagePath = getFieldImagePath(fieldId);
        if (imagePath != null) {
            // Naloži sliko iz poti
            ImageIcon imageIcon = new ImageIcon(imagePath);
            return imageIcon; // Vrni sliko kot Icon
        }
        return null; // Če slike ni, vrni null
    }

    private boolean updateAdminStatus(int userId, boolean newAdminStatus, DatabaseManager dbManager) {
        String query = "UPDATE users SET admin = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, newAdminStatus); // Nastavi nov status admina
            stmt.setInt(2, userId); // Nastavi ID uporabnika

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Če je bilo spremenjenih več kot 0 vrstic, je bila sprememba uspešna
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";  // SQL query to fetch all users

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String ime = rs.getString("ime");
                String priimek = rs.getString("priimek");
                String email = rs.getString("email");
                String geslo = rs.getString("geslo");
                boolean admin = rs.getBoolean("admin"); // ✅ Popravljeno
                String telefon = rs.getString("telefonska"); // Fetch the telefon from the database
                String naslov = rs.getString("naslov"); // Fetch the naslov from the database

                // Now include telefon and naslov when creating the User object
                User user = new User(id, ime, priimek, email, geslo, admin, telefon, naslov);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }



    // Method to delete user
    public void deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int getUserIdByEmail(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        int userId = -1;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);  // Set the email parameter correctly
            ResultSet rs = stmt.executeQuery();  // Execute the query

            if (rs.next()) {  // Check if the result set has a valid row
                userId = rs.getInt("id");  // Retrieve the user ID
                System.out.println("Database returned user ID: " + userId);  // Debugging: Print the user ID
            } else {
                System.out.println("No user found with email: " + email);  // Debugging
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userId;
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

    public boolean jeIgrisceProsto(int igrisceId, Timestamp zacetek, Timestamp konec) {
        String sql = "SELECT COUNT(*) FROM rezervacije WHERE igrisce_id = ? AND " +
                "( (zacetek < ? AND konec > ?) OR (zacetek >= ? AND zacetek < ?) )"; // Popravljen SQL stavek

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, igrisceId);
            stmt.setTimestamp(2, konec);  // konec rezervacije
            stmt.setTimestamp(3, zacetek);  // začetek rezervacije
            stmt.setTimestamp(4, zacetek);  // tvoj predlagani začetek
            stmt.setTimestamp(5, konec);  // tvoj predlagani konec

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;  // Če je število rezervacij 0, potem je igrišče prosto
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean rezervirajIgrisce(int igrisceId, Timestamp zacetek, Timestamp konec) {
        // Pridobi ID trenutnega uporabnika iz Session (lahko nastaviš pravilno metodo za pridobivanje uporabnika)
        int userId = Session.getCurrentUserId();

        // Preveri, ali je uporabnik prijavljen
        if (userId == -1) {
            System.out.println("Napaka: Uporabnik ni prijavljen!");
            return false;
        }

        // Preveri, ali uporabnik obstaja v tabeli "users"
        if (!uporabnikObstaja(userId)) {
            System.out.println("Napaka: Uporabnik s tem ID-jem ne obstaja!");
            return false;
        }

        // Vstavi rezervacijo, ker je uporabnik veljaven
        try {
            String sql = "INSERT INTO rezervacije (zacetek, konec, user_id, igrisce_id) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, zacetek);
            stmt.setTimestamp(2, konec);
            stmt.setInt(3, userId);  // Nastavi user_id na trenutni ID uporabnika
            stmt.setInt(4, igrisceId);
            stmt.executeUpdate();
            System.out.println("Rezervacija uspešno dodana!");
            return true;
        } catch (SQLException e) {
            System.out.println("Napaka pri dodajanju rezervacije: " + e.getMessage());
            return false;
        }
    }

    // Funkcija, ki preveri, ali uporabnik obstaja v tabeli "users"
    private boolean uporabnikObstaja(int userId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Če je več kot 0, uporabnik obstaja
            }
        } catch (SQLException e) {
            System.out.println("Napaka pri preverjanju uporabnika: " + e.getMessage());
        }
        return false;  // Če ni uporabnika
    }
    public List<Reservation> getRezervacijeUporabnika(int userId) {
        List<Reservation> rezervacije = new ArrayList<>();
        String query = "SELECT r.id, r.zacetek, r.konec, r.igrisce_id, i.kraj_id " +
                "FROM rezervacije r JOIN igrisca i ON r.igrisce_id = i.id " +
                "WHERE r.user_id = ? ORDER BY r.zacetek DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("id"),
                        rs.getTimestamp("zacetek"),
                        rs.getTimestamp("konec"),
                        rs.getInt("igrisce_id"),
                        rs.getInt("kraj_id")
                );
                rezervacije.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rezervacije;
    }

    public String getImeIgriscaById(int igrisceId) {
        String ime = "";
        String query = "SELECT ime FROM igrisca WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, igrisceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ime = rs.getString("ime");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ime;
    }
    public int getKrajIdByIgrisceId(int igrisceId) {
        int krajId = -1;

        String sql = "SELECT kraj_id FROM igrisca WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, igrisceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    krajId = rs.getInt("kraj_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return krajId;
    }


}
