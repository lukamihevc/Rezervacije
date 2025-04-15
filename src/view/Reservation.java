package view;

import java.sql.Timestamp;


public class Reservation {
    private int id;
    private Timestamp zacetek;
    private Timestamp konec;
    private int igrisceId;
    private int krajId;

    public Reservation (int id, Timestamp zacetek, Timestamp konec, int igrisceId, int krajId) {
        this.id = id;
        this.zacetek = zacetek;
        this.konec = konec;
        this.igrisceId = igrisceId;
        this.krajId = krajId;
    }

    public int getId() { return id; }
    public Timestamp getZacetek() { return zacetek; }
    public Timestamp getKonec() { return konec; }
    public int getIgrisceId() { return igrisceId; }
    public int getKrajId() { return krajId; }
    }

