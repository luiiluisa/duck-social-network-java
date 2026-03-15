package model;

import model.Enums.TipDuck;
import model.capabilitati.Zburator;

/**
 * Rata care poate doar zbura
 */
public class FlyingDuck extends Duck implements Zburator {

    public FlyingDuck(Long id, String username, String email, String password,
                      double viteza, double rezistenta) {
        super(id, username, email, password, TipDuck.FLYING, viteza, rezistenta);
    }

    @Override
    public void zboara() {
        System.out.println(getUsername() + " zboara cu viteza " + getViteza());
    }
}
