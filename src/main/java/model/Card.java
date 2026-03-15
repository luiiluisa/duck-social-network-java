package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezinta un card de rate din rețea
 */
public class Card {
    private Long id;
    private String numeCard;
    private final List<Duck> membri = new ArrayList<>();

    public Card(String numeCard) {
        this.numeCard = numeCard;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeCard() {
        return numeCard;
    }

    public void setNumeCard(String numeCard) {
        this.numeCard = numeCard;
    }

    /**
     * Returneaza o copie a listei de membri
     */
    public List<Duck> getMembri() {
        return new ArrayList<>(membri);
    }

    /**
     * Adaugă o rata în card, evitand duplicatele si sincronizand relația bidirecționala
     *
     * @param d rata de adaugat
     * @return true daca a fost adaugata cu succes
     */
    public boolean addDuck(Duck d) {
        if (d == null) return false;
        if (membri.contains(d)) return false;

        if (d.getCard() != null && d.getCard() != this) {
            d.getCard().removeDuck(d);
        }

        membri.add(d);
        d.setCard(this);
        return true;
    }

    /**
     * Elimina o rata din card si sincronizeaza relația inversa
     *
     * @param d rata de eliminat
     * @return true daca a fost eliminata cu succes
     */
    public boolean removeDuck(Duck d) {
        if (d == null) return false;
        boolean ok = membri.remove(d);
        if (ok && d.getCard() == this) {
            d.setCard(null);
        }
        return ok;
    }

    /**
     * Calculeaza performanța medie a membrilor (viteza + rezistența) / 2
     *
     * @return performanța medie a cardului
     */
    public double getPerformantaMedie() {
        if (membri.isEmpty()) return 0.0;
        double s = 0.0;
        for (Duck d : membri) {
            s += (d.getViteza() + d.getRezistenta()) / 2.0;
        }
        return s / membri.size();
    }
}
