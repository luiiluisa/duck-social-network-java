package model;

import java.time.LocalDateTime;
import java.util.List;

public class Message {
    private Long id;
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime data;

    public Message(Long id, User from, List<User> to, String message, LocalDateTime data) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
        this.data = data;
    }

    public Long getId() { return id; }
    public User getFrom() { return from; }
    public List<User> getTo() { return to; }
    public String getMessage() { return message; }
    public LocalDateTime getData() { return data; }
    public void setId(Long id) { this.id = id; }

}
