🏟️ Aplikacija za rezervacijo športnih igrišč

📌 Opis

To je namizna Java aplikacija, ki omogoča registracijo uporabnikov, prijavo, in rezervacijo športnih igrišč. Projekt uporablja PostgreSQL bazo podatkov, strežniške podprograme in triggerje za zagotavljanje podatkovne integritete in beleženje dogodkov. Uporabniki lahko rezervirajo termin, administratorji pa lahko upravljajo z igrišči in uporabniki.

✨ Glavne funkcionalnosti

✅ Registracija in prijava uporabnikov (gesla so šifrirana)

✅ Uporabniški vmesnik v Javi (brez FXML)

✅ Možnost dodajanja, urejanja in brisanja rezervacij

✅ Ločeno upravljanje s pravicami administratorjev

✅ Preprečevanje podvajanja terminov znotraj baze

✅ Uporaba JDatePicker za izbiro datumov

✅ Avtomatski logi sprememb in brisanj

📃 Struktura projekta
Rezervacije/
├── src/
│   ├── controller/
│   ├── db/
│   │   └── DatabaseManager.java
│   ├── model/
│   ├── sportsbookingapp/
│   └── view/
├── README.md
└── baza/
    └── setup.sql

🧠 Strežniški podprogrami in triggerji

Luka – moji strežniški podprogrami in triggerji

1. Brisanje uporabnika
CREATE OR REPLACE FUNCTION delete_user(p_user_id INT)
RETURNS VOID AS $$
BEGIN
    DELETE FROM users WHERE id = p_user_id;
END;
$$ LANGUAGE plpgsql;

2. Sprememba admin statusa uporabnika

CREATE OR REPLACE FUNCTION update_admin_status(p_user_id INT, p_new_admin_status BOOLEAN)
RETURNS VOID AS $$
BEGIN
    UPDATE users
    SET admin = p_new_admin_status
    WHERE id = p_user_id;
END;
$$ LANGUAGE plpgsql;

3. Logiranje sprememb admin statusa

CREATE TABLE admin_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    old_admin_status BOOLEAN,
    new_admin_status BOOLEAN,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION log_admin_status_change()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO admin_logs (user_id, old_admin_status, new_admin_status, change_date)
    VALUES (NEW.id, OLD.admin, NEW.admin, CURRENT_TIMESTAMP);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER admin_status_update_trigger1
AFTER UPDATE OF admin ON users
FOR EACH ROW
WHEN (OLD.admin IS DISTINCT FROM NEW.admin)
EXECUTE FUNCTION log_admin_status_change();

4. Logiranje novega uporabnika
CREATE OR REPLACE FUNCTION log_user_insert()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO admin_logs (action, user_id)
    VALUES ('User Added', NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_insert_trigger
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION log_user_insert();

 Nea – dodatni strežniški podprogrami in triggerji

1. Dodajanje rezervacije (preverjanje prekrivanja)

CREATE OR REPLACE FUNCTION dodaj_rezervacijo(
    IN p_igrisce_id INT,
    IN p_user_id INT,
    IN p_zacetek TIMESTAMP,
    IN p_konec TIMESTAMP) RETURNS BOOLEAN AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM rezervacija
        WHERE igrisce_id = p_igrisce_id
          AND ((zacetek < p_konec AND konec > p_zacetek))
    ) THEN
        RETURN FALSE;
    END IF;

    INSERT INTO rezervacija (zacetek, konec, user_id, igrisce_id)
    VALUES (p_zacetek, p_konec, p_user_id, p_igrisce_id);
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

2. Sprememba datuma rezervacije (z validacijo)

CREATE OR REPLACE FUNCTION spremeni_datum_rezervacije(
    IN p_rezervacija_id INT,
    IN p_nov_zacetek TIMESTAMP,
    IN p_nov_konec TIMESTAMP) RETURNS BOOLEAN AS $$
DECLARE
    v_igrisce_id INT;
BEGIN
    SELECT igrisce_id INTO v_igrisce_id FROM rezervacija WHERE id = p_rezervacija_id;

    IF EXISTS (
        SELECT 1 FROM rezervacija
        WHERE igrisce_id = v_igrisce_id
          AND id != p_rezervacija_id
          AND (zacetek < p_nov_konec AND konec > p_nov_zacetek)
    ) THEN
        RETURN FALSE;
    END IF;

    UPDATE rezervacija
    SET zacetek = p_nov_zacetek,
        konec = p_nov_konec
    WHERE id = p_rezervacija_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

1. Trigger za preverjanje prekrivanja rezervacij

CREATE OR REPLACE FUNCTION preveri_prekrivanje() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM rezervacija
        WHERE igrisce_id = NEW.igrisce_id
          AND (zacetek < NEW.konec AND konec > NEW.zacetek)
          AND id != NEW.id
    ) THEN
        RAISE EXCEPTION 'Igrisce je že rezervirano za izbrani termin.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prekrivanje
BEFORE INSERT OR UPDATE ON rezervacija
FOR EACH ROW EXECUTE FUNCTION preveri_prekrivanje();

2. Trigger za logiranje izbrisanih rezervacij

CREATE TABLE rezervacija_log (
    rezervacija_id INT,
    user_id INT,
    igrisce_id INT,
    zacetek TIMESTAMP,
    konec TIMESTAMP,
    izbrisana_ob TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION log_izbrisane_rezervacije() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO rezervacija_log (rezervacija_id, user_id, igrisce_id, zacetek, konec)
    VALUES (OLD.id, OLD.user_id, OLD.igrisce_id, OLD.zacetek, OLD.konec);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_izbris
AFTER DELETE ON rezervacija
FOR EACH ROW EXECUTE FUNCTION log_izbrisane_rezervacije();
