package service;

import model.Event;
import model.User;
import model.observer.Observer;
import repository.BDrepo.EventBDRepository;
import repository.UserRepository;
import validator.EventValidation;
import java.util.ArrayList;

import java.util.List;

public class EventService {

    private EventBDRepository eventRepository;
    private UserRepository userRepository;
    private EventValidation eventValidation;

    public EventService(EventBDRepository eventRepository,
                        UserRepository userRepository,
                        EventValidation eventValidation) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventValidation = eventValidation;
    }

    /**
     * Creeaza un eveniment daca trece de validare si il salveaza in BD.
     */
    public boolean createEvent(Event e) {
        if (e == null) return false;
        if (eventValidation.validate(e)) {
            return eventRepository.save(e);
        }
        return false;
    }

    /**
     * Sterge evenimentul din BD (si toti abonatii lui).
     */
    public boolean deleteEvent(Event e) {
        return eventRepository.delete(e);
    }

    /**
     * Aboneaza utilizatorul cu id dat la eveniment.
     */
    public boolean subscribe(Event e, Long userId) {
        if (e == null || userId == null) return false;
        User u = get(userId);
        if (u == null) return false;
        return eventRepository.subscribe(e, u);
    }

    /**
     * Dezaboneaza utilizatorul cu id dat de la eveniment.
     */
    public boolean unsubscribe(Event e, Long userId) {
        if (e == null || userId == null) return false;
        User u = get(userId);
        if (u == null) return false;
        return eventRepository.unsubscribe(e, u);
    }

    /**
     * Notifica toti abonatii evenimentului (abonatii sunt luati din BD).
     */
    public void notifyAll(Event e, String message) {
        if (e == null || message == null || message.isBlank()) return;
        List<Observer> subs = eventRepository.listSubscribers(e);
        if (subs == null || subs.isEmpty()) return;
        for (Observer o : subs) {
            if (o != null) {
                o.update(message);
            }
        }
    }

    /**
     * Toate evenimentele din BD.
     */
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    private User get(Long id) {
        if (id == null) return null;
        for (User u : userRepository.findAll()) {
            if (id.equals(u.getId())) return u;
        }
        return null;
    }

    /**
     * Toti utilizatorii abonati la un eveniment (din BD).
     * Foloseste event_subscribers si reconstruieste User pentru fiecare id.
     */
    public List<User> listSubscribedUsers(Event e) {
        List<User> result = new ArrayList<>();
        if (e == null) return result;

        List<Observer> subs = eventRepository.listSubscribers(e);
        if (subs == null) return result;

        for (Observer o : subs) {
            if (o instanceof User u) {
                result.add(u);
            }
        }
        return result;
    }

    public List<Event> eventsForUser(Long userId) {
        return eventRepository.findEventsForUser(userId);
    }

    public List<Event> findSubscribedEvents(Long userId) {
        return eventRepository.findSubscribedEvents(userId);
    }

    public boolean isSubscribed(Long eventId, Long userId) {
        return eventRepository.isUserSubscribed(eventId, userId);
    }


}
