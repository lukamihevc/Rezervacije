package model;

public class User {
    private int id;
    private String ime;
    private String priimek;
    private String email;
    private String geslo;
    private boolean admin;
    private String telefon;
    private String naslov;

    // Constructor
    public User(int id, String ime, String priimek, String email, String geslo, boolean admin, String telefon, String naslov) {
        this.id = id;
        this.ime = ime;
        this.priimek = priimek;
        this.email = email;
        this.geslo = geslo;
        this.admin = admin;
        this.telefon = telefon;
        this.naslov = naslov;
    }

    // Getters and Setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPriimek() {
        return priimek;
    }

    public void setPriimek(String priimek) {
        this.priimek = priimek;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGeslo() {
        return geslo;
    }

    public void setGeslo(String geslo) {
        this.geslo = geslo;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getTelefon() {
        return telefon; // Add the getter for telefon
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getNaslov() {
        return naslov; // Add the getter for naslov
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }
}
