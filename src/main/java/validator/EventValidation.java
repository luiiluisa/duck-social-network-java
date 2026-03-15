package validator;

import exceptions.ValidationException;
import model.Event;

public class EventValidation implements ValidationStrategy<Event> {
    @Override
    public boolean validate(Event e) {
        if (e == null)
            throw new ValidationException("Event null");
        if (e.getSubscribers() == null)
            throw new ValidationException("Lista subscriberi null");
        return true;
    }
}
