package repository;

import model.Event;
import model.User;
import model.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository in-memory pentru evenimente generice.
 */
public class EventRepository {

    private List<Event> events = new ArrayList<>();

    /**
     * Salvează un eveniment (append).
     * @return true dacă s-a adăugat
     */
    public boolean save(Event e) {
        if (e == null) return false;
        return events.add(e);
    }

    /**
     * Șterge un eveniment
     * @return true dacă s-a eliminat
     */
    public boolean delete(Event e) {
        if (e == null) return false;
        return events.remove(e);
    }

    /**
     * Abonează un utilizator la eveniment
     */
    public boolean subscribe(Event e, User u) {
        if (e == null || u == null) return false;
        e.subscribe(u);
        return true;
    }

    /**
     * Dezabonează un utilizator
     */
    public boolean unsubscribe(Event e, User user) {
        if (e == null || user == null) return false;
        e.unsubscribe(user);
        return true;
    }

    /**
     * Listează abonații unui eveniment.
     */
    public List<Observer> listSubscribers(Event e) {
        if (e == null) return new ArrayList<>();
        return new ArrayList<>(e.getSubscribers());
    }

}
