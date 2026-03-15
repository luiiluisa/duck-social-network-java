package factory;

import model.*;
import model.Enums.TipDuck;

import java.time.LocalDate;

public class UserFactory {

    public User createFromCsv(String line) {
        if (line == null) return null;

        String[] p = line.split(",");
        for (int i = 0; i < p.length; i++) p[i] = p[i].trim();
        if (p.length < 4) return null;

        String kind = p[0].toUpperCase();

        if ("PERSON".equals(kind)) {

            if (p.length < 9) return null;

            String username = p[1];
            String email = p[2];
            String password = p[3];
            String nume = p[4];
            String prenume = p[5];
            String dnStr = p[6];
            String ocupatie = p[7];
            String empStr = p[8];

            LocalDate dn = dnStr.isBlank() ? null : parseDate(dnStr);
            double emp = empStr.isBlank() ? 0.0 : parseDouble(empStr);

            // ID = null → va fi generat automat în UserRepository
            return new Person(null, username, email, password, nume, prenume, dn, ocupatie, emp);
        }

        if ("DUCK".equals(kind)) {

            if (p.length < 7) return null;

            String username = p[1];
            String email = p[2];
            String password = p[3];
            String tipStr = p[4];
            String vStr = p[5];
            String rStr = p[6];

            TipDuck tip = tipStr.isBlank() ? null : parseTip(tipStr);
            double v = vStr.isBlank() ? 0.0 : parseDouble(vStr);
            double r = rStr.isBlank() ? 0.0 : parseDouble(rStr);

            // ID = null => generat automat
            if (tip == TipDuck.FLYING) {
                return new FlyingDuck(null, username, email, password, v, r);
            } else {
                return new SwimmingDuck(null, username, email, password, v, r);
            }
        }

        return null;
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }

    private TipDuck parseTip(String s) {
        try { return TipDuck.valueOf(s.toUpperCase()); }
        catch (Exception e) { return null; }
    }
}
