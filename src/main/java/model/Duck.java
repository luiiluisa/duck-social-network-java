package model;

import model.Enums.TipDuck;

/**
 * Reprezinta un utilizator de tip rata
 */
public abstract class Duck extends User {
    private TipDuck tip;
    private double viteza;
    private double rezistenta;
    private Card card;

    public Duck(Long id, String username, String email, String password,
                TipDuck tip, double viteza, double rezistenta) {
        super(id, username, email, password);
        this.tip = tip;
        this.viteza = viteza;
        this.rezistenta = rezistenta;
    }

    public TipDuck getTip() { return tip; }
    public double getViteza() { return viteza; }
    public double getRezistenta() { return rezistenta; }
    public Card getCard() { return card; }

    public void setTip(TipDuck tip) { this.tip = tip; }
    public void setViteza(double viteza) { this.viteza = viteza; }
    public void setRezistenta(double rezistenta) { this.rezistenta = rezistenta; }
    public void setCard(Card card) { this.card = card; }
}
