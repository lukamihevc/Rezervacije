package model;

public class Field {
    private int id;
    private String ime;
    private String sport;
    private String slika;
    private int krajId;
    private int kapaciteta;

    public Field(int id, String ime, String sport, String slika, int krajId, int kapaciteta) {
        this.id = id;
        this.ime = ime;
        this.sport = sport;
        this.slika = slika;
        this.krajId = krajId;
        this.kapaciteta = kapaciteta;
    }

    public int getId() {
        return id;
    }

    public String getIme() {
        return ime;
    }

    public String getSport() {
        return sport;
    }

    public String getSlika() {
        return slika;
    }

    public int getKrajId() {
        return krajId;
    }

    public int getKapaciteta() {
        return kapaciteta;
    }
}
