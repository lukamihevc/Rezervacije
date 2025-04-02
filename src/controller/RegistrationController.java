package controller;

import db.DatabaseManager;

public class RegistrationController {

    public boolean register(String firstName, String lastName, String password, String email, String phone, String address) {
        // Preveri, če uporabnik že obstaja
        DatabaseManager dbManager = new DatabaseManager();
        return dbManager.registerUser(firstName, lastName, password, email, phone, address);
    }
}
