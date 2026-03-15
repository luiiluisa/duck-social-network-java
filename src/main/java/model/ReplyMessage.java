package model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReplyMessage = un mesaj care raspunde la un alt mesaj.
 * Extinde Message si adauga referinta catre mesajul la care raspunde.
 */
public class ReplyMessage extends Message {

    private Message repliedTo; // mesajul la care raspunde

    public ReplyMessage(Long id,
                        User from,
                        List<User> to,
                        String message,
                        LocalDateTime data,
                        Message repliedTo) {
        super(id, from, to, message, data);
        this.repliedTo = repliedTo;
    }

    public Message getRepliedTo() {
        return repliedTo;
    }

    public void setRepliedTo(Message repliedTo) {
        this.repliedTo = repliedTo;
    }
}
