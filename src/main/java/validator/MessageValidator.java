package validator;

import exceptions.ValidationException;
import model.Message;
import model.User;

public class MessageValidator implements ValidationStrategy<Message> {

    @Override
    public boolean validate(Message m) {
        if (m == null)
            throw new ValidationException("Message null");

        // from
        if (m.getFrom() == null || m.getFrom().getId() == null)
            throw new ValidationException("Sender invalid");

        // to (lista)
        if (m.getTo() == null || m.getTo().isEmpty())
            throw new ValidationException("No receivers");

        for (User u : m.getTo()) {
            if (u == null || u.getId() == null)
                throw new ValidationException("Invalid receiver");
            if (u.equals(m.getFrom()))
                throw new ValidationException("Sender cannot send message to himself");
        }

        // mesaj
        if (m.getMessage() == null || m.getMessage().trim().isEmpty())
            throw new ValidationException("Empty message");

        // data poate fi null (se seteaza automat la save)
        return true;
    }
}
