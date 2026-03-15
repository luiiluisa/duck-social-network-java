package model.observer;

/**
 * Subject: permite abonarea / dezabonarea Observer-ului si trimiterea de notificari
 */
public interface Subject {
    void subscribe(Observer o);
    void unsubscribe(Observer o);
    void notifySubscribers(String message);
}
