package repository;

import model.Message;
import model.User;

import java.util.*;

/**
 * In-memory repository pentru mesaje.
 * Cheia este id-ul mesajului.
 */
public class MessageRepository {

    private Map<Long, Message> data = new HashMap<>();
    private long nextId = 1;

    /**
     * Salveaza un mesaj nou sau il suprascrie daca exista.
     * Daca mesajul nu are id, ii genereaza unul.
     */
    public Message save(Message m) {
        if (m == null) return null;

        if (m.getId() == null) {
            try {
                // setam id prin reflexie sau constructor nou
                m = new Message(
                        nextId++,
                        m.getFrom(),
                        m.getTo(),
                        m.getMessage(),
                        m.getData()
                );
            } catch (Exception e) {
                return null;
            }
        }

        data.put(m.getId(), m);
        return m;
    }

    /**
     * Sterge un mesaj dupa id.
     * @return true daca mesajul a existat
     */
    public boolean delete(Long id) {
        if (id == null) return false;
        return data.remove(id) != null;
    }

    /**
     * Toate mesajele trimise de un utilizator.
     */
    public List<Message> findByUserSent(User sender) {
        List<Message> list = new ArrayList<>();
        if (sender == null) return list;

        for (Message m : data.values()) {
            if (sender.equals(m.getFrom())) {
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Toate mesajele primite de un utilizator.
     * Un mesaj este considerat primit daca utilizatorul
     * se afla in lista "to".
     */
    public List<Message> findByUserReceived(User receiver) {
        List<Message> list = new ArrayList<>();
        if (receiver == null) return list;

        for (Message m : data.values()) {
            if (m.getTo() != null && m.getTo().contains(receiver)) {
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Conversatia dintre doi utilizatori.
     * Include mesajele trimise de A catre B si de B catre A.
     */
    public List<Message> findConversation(User a, User b) {
        List<Message> list = new ArrayList<>();
        if (a == null || b == null) return list;

        for (Message m : data.values()) {
            boolean aToB = a.equals(m.getFrom())
                    && m.getTo() != null
                    && m.getTo().contains(b);

            boolean bToA = b.equals(m.getFrom())
                    && m.getTo() != null
                    && m.getTo().contains(a);

            if (aToB || bToA) {
                list.add(m);
            }
        }

        list.sort(Comparator.comparing(Message::getData));
        return list;
    }
}
