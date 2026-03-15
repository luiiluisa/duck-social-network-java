package repository;

import model.Card;
import model.Duck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository in-memory pentru Card
 * Cheia este id-ul cardului
 */
public class CardRepository {

    private Map<Long, Card> data = new HashMap<>();
    private long nextId = 1;
    /**
     * Salveaza sau suprascrie un card dupa id.
     * @return cardul salvat, sau null daca e invalid
     */
    public Card save(Card c) {
        if (c == null) return null;

        if (c.getId() == null) {
            c.setId(nextId++);
        }

        data.put(c.getId(), c);
        return c;
    }


    /**
     * Șterge un card după id.
     * @return true dacă s-a șters ceva
     */
    public boolean delete(Long id) {
        if (id == null) return false;
        Card c = data.remove(id);
        if (c == null) return false;
        for (Duck d : new ArrayList<>(c.getMembri())) {
            c.removeDuck(d);
        }
        return true;
    }


    /**
     * Găsește un card după id.
     */
    public Card findById(Long id) {
        if (id == null) return null;
        return data.get(id);
    }

    /**
     * Returnează toate cardurile.
     */
    public List<Card> findAll() {
        return new ArrayList<>(data.values());
    }

    /**
     * Adaugă o rață în cardul cu id dat.
     * @return true dacă a reușit
     */
    public boolean addDuckToCard(Long cardId, Duck d) {
        Card c = findById(cardId);
        if (c == null || d == null) return false;
        return c.addDuck(d);
    }

    /**
     * Elimina o rata in cardul cu id dat.
     * @return true dacă a reușit
     */
    public boolean removeDuckFromCard(Long cardId, Duck d) {
        Card c = findById(cardId);
        if (c == null || d == null) return false;
        return c.removeDuck(d);
    }

    /**
     * Elimina o rata din membri unui card
     * @return lista*/
    public int removeDuckEverywhereById(Long duckId) {
        if (duckId == null) return 0;
        int removed = 0;
        for (Card c : data.values()) {
            for (Duck d : new ArrayList<>(c.getMembri())) {
                if (duckId.equals(d.getId())) {
                    if (c.removeDuck(d)) removed++;
                }
            }
        }
        return removed;
    }


    /**
     * Listeaza membrii cardului.
     * Daca cardul nu exista, întoarce lista goala
     */
    public List<Duck> listDucksInCard(Long cardId) {
        Card c = findById(cardId);
        if (c == null) return new ArrayList<>();
        return new ArrayList<>(c.getMembri());
    }
}
