package model;

import model.Enums.TipDuck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Eveniment de tip cursa de natație
 *  - distante: d1 >= d2 >= ...
 *  - participante: M rate
 *  - timp pe culoar: t = 2 * d / v
 *  - timp cursa
 */
public class RaceEvent extends Event {
    private final List<Double> distante = new ArrayList<>();
    private final List<Duck> participante = new ArrayList<>();

    public List<Double> getDistante() { return new ArrayList<>(distante); }
    public List<Duck> getParticipante() { return new ArrayList<>(participante); }

    /** Seteaza distanțele: validează (>=0) și ordoneaza descrescător (greedy: cea mai buna rata pe cea mai lunga distanta) */
    public void setDistante(List<Double> ds) {
        distante.clear();
        if (ds == null) return;
        for (Double d : ds) {
            if (d != null && d >= 0) distante.add(d);
        }
        distante.sort(Comparator.<Double>naturalOrder().reversed());
    }

    /** Curata lista de participante. */
    public void clearParticipante() {
        participante.clear();
    }

    /**
     * Alege automat M rate care pot inota si au viteza > 0,
     * ordonate desc dupa rezis., apoi desc dupa viteza
     *
     */
    public void selecteazaParticipante(List<Duck> toateRatele) {
        participante.clear();
        if (toateRatele == null || distante.isEmpty()) return;

        int M = distante.size(); //nr de dist.
        List<Duck> cand = new ArrayList<>();
        for (Duck d : toateRatele) {
            if (d == null) continue;
            TipDuck t = d.getTip();
            if ((t == TipDuck.SWIMMING) && d.getViteza() > 0) {
                cand.add(d);
            }
        }

        cand.sort(Comparator
                .comparingDouble(Duck::getRezistenta).reversed()
                .thenComparing(Comparator.comparingDouble(Duck::getViteza).reversed()));

        for (int i = 0; i < Math.min(M, cand.size()); i++) {
            participante.add(cand.get(i));
        }
    }

    /**
     * Ruleaza simulare
     */
    public List<String> simuleaza() {
        List<String> out = new ArrayList<>();
        int M = distante.size();
        if (M == 0) {
            out.add("Nu sunt distanțe setate.");
            return out;
        }
        if (participante.size() < M) {
            out.add("Nu sunt suficiente rate selectate pentru numarul de distante");
            out.add("Necesare: " + M + ", selectate: " + participante.size());
            return out;
        }

        double timpTotal = 0.0;
        for (int i = 0; i < M; i++) {
            Duck r = participante.get(i);
            double d = distante.get(i);
            double v = r.getViteza();
            if (v <= 0) {
                out.add("Eroare: " + r.getUsername() + " are viteză <1, simularea nu poate continua");
                return out;
            }
            double t = (2.0 * d) / v;

            timpTotal = Math.max(timpTotal, t);
            out.add(String.format(
                    "Lane %d: %s  d=%.2f  v=%.2f  rezistenta=%.2f  timp=%.2fs",
                    (i + 1), r.getUsername(), d, r.getViteza(), r.getRezistenta(), t
            ));
        }
        out.add(String.format("Timp cursa = %.2fs", timpTotal));
        return out;
    }
}
