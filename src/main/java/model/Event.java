package model;

import model.observer.Observer;
import model.observer.Subject;

import java.util.ArrayList;
import java.util.List;

public class Event implements Subject {

    private Long id;
    private final List<Observer> subscribers = new ArrayList<>();

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Observer> getSubscribers() {
        return subscribers;
    }

    @Override
    public void subscribe(Observer o) {
        if (o != null && !subscribers.contains(o)) {
            subscribers.add(o);
        }
    }

    @Override
    public void unsubscribe(Observer o) {
        subscribers.remove(o);
    }

    @Override
    public void notifySubscribers(String message) {
        if (message == null || message.isBlank()) return;
        for (Observer o : subscribers) {
            if (o == null) continue;
            o.update(message);
        }
    }

    public void subscribeUser(User user) {
        subscribe(user);
    }

    public void unsubscribeUser(User user) {
        unsubscribe(user);
    }
}
