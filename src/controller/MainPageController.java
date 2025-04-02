package controller;

import java.util.ArrayList;
import java.util.List;

public class MainPageController {

    // Funkcija, ki bo vrnila seznam igrišč (stadionov)
    public List<String> getStadiums() {
        List<String> stadiums = new ArrayList<>();

        // Tukaj bi pridobili igrišča iz baze podatkov. Trenutno samo simuliramo.
        stadiums.add("Stadion A");
        stadiums.add("Stadion B");
        stadiums.add("Stadion C");

        return stadiums;
    }
}
