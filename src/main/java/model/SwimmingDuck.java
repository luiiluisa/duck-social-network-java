package model;

import model.Enums.TipDuck;
import model.capabilitati.Inotator;

/**
 * Rata care poate doar înota
 */
public class SwimmingDuck extends Duck implements Inotator {

    public SwimmingDuck(Long id, String username, String email, String password,
                        double viteza, double rezistenta) {
        super(id, username, email, password, TipDuck.SWIMMING, viteza, rezistenta);
    }

    @Override
    public void inoata() {
        System.out.println(getUsername() + " inoata cu viteza " + getViteza());
    }

}
