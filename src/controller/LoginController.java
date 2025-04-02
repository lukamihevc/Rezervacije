package controller;

import db.DatabaseManager;

public class LoginController {

    public boolean login(String email, String password) {
        DatabaseManager dbManager = new DatabaseManager();

        // Preverjanje, ali je uporabnik admin
        if (dbManager.isAdmin(email)) {  // Tukaj uporabimo email namesto uporabniškega imena
            if (dbManager.validateUser(email, password)) {  // Preverimo uporabniško ime (email) in geslo
                return true;  // Uporabnik je admin in geslo je pravilno
            } else {
                return false; // Napačno geslo
            }
        }

        // Preverimo za običajnega uporabnika
        return dbManager.validateUser(email, password);  // Za običajnega uporabnika preverimo geslo
    }
}
