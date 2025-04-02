package sportsbookingapp;

import view.LoginPage;
import db.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        // Testiranje povezave z bazo
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.testConnection();  // Preverjanje povezave

        // Zaženite LoginPage
        new LoginPage();
    }
}
