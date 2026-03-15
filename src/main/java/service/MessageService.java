package service;

import model.Message;
import model.User;
import repository.BDrepo.MessageBDRepository;
import repository.UserRepository;
import validator.MessageValidator;

import java.util.List;

/**
 * Business logic pentru mesaje:
 * - validare
 * - send / delete
 * - inbox / outbox
 */
public class MessageService {

    private final MessageBDRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageValidator messageValidator;

    public MessageService(MessageBDRepository messageRepository,
                          UserRepository userRepository,
                          MessageValidator messageValidator) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageValidator = messageValidator;
    }

    /**
     * Trimite (salveaza) un mesaj daca trece de validare si daca utilizatorii exista.
     * Observatie: mesajul are from + to(List<User>).
     */
    public Message send(Message m) {
        if (m == null) return null;

        if (!exists(m.getFrom())) return null;

        if (m.getTo() == null || m.getTo().isEmpty()) return null;

        // toti destinatarii trebuie sa existe
        for (User u : m.getTo()) {
            if (!exists(u)) return null;
        }

        if (messageValidator.validate(m)) {
            return messageRepository.save(m);
        }
        return null;
    }

    /**
     * Sterge un mesaj dupa id.
     */
    public boolean delete(Long messageId) {
        return messageRepository.delete(messageId);
    }

    /**
     * Inbox: mesajele primite de utilizatorul cu id dat.
     */
    public List<Message> inbox(Long userId) {
        User u = get(userId);
        if (u == null) return List.of();
        return messageRepository.findByUserReceived(u);
    }

    /**
     * Outbox: mesajele trimise de utilizatorul cu id dat.
     */
    public List<Message> outbox(Long userId) {
        User u = get(userId);
        if (u == null) return List.of();
        return messageRepository.findByUserSent(u);
    }

    /**
     * Verifica daca un user exista in BD
     */
    private boolean exists(User u) {
        if (u == null || u.getId() == null) return false;

        User found = userRepository.findById(u.getId());
        return found != null;
    }

    /**
     * Intoarce userul dupa id sau null daca nu exista.
     */
    private User get(Long id) {
        if (id == null) return null;
        return userRepository.findById(id);
    }

    //mesajele dintre doi user
    public List<Message> conversation(Long aId, Long bId) {
        User a = userRepository.findById(aId);
        User b = userRepository.findById(bId);
        if (a == null || b == null) return List.of();
        return messageRepository.findConversation(a, b);
    }

}
