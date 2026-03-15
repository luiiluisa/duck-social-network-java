package factory;

import model.Event;
import model.RaceEvent;

public class EventFactory {

    public Event createEvent(String type) {
        if (type == null) return null;
        if ("RACE".equalsIgnoreCase(type)) {
            return new RaceEvent();
        }
        return new Event();
    }
}
