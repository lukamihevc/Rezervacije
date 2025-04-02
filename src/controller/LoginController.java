package controller;

import db.DatabaseManager;

public class LoginController {
    public boolean login(String email, String password) {
        DatabaseManager dbManager = new DatabaseManager();
        return dbManager.checkLogin(email, password);
    }
}
