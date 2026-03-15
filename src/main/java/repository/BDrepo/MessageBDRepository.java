package repository.BDrepo;

import model.Message;
import model.ReplyMessage;
import model.User;
import repository.ConexiuneBD;
import repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository BD pentru mesaje:
 *  - tabela messages(id, from_id, message, data, reply_to_id)
 *  - tabela message_to(message_id, to_id)
 */
public class MessageBDRepository {

    private final UserRepository userRepository;

    public MessageBDRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Salveaza un mesaj nou in BD.
     * - id e BIGSERIAL (se genereaza in BD)
     * - insereaza in messages + message_to (cate un rand pentru fiecare destinatar)
     * Returneaza mesajul cu id setat.
     */
    public Message save(Message m) {
        //validari
        if (m == null) return null;
        if (m.getFrom() == null || m.getFrom().getId() == null) return null;
        if (m.getTo() == null || m.getTo().isEmpty()) return null;
        if (m.getMessage() == null || m.getMessage().isBlank()) return null;

        for (User u : m.getTo()) {
            if (u == null || u.getId() == null) return null;
        }

        //daca e reply
        Long replyToId = null;
        if (m instanceof ReplyMessage rm) {
            if (rm.getRepliedTo() == null || rm.getRepliedTo().getId() == null) return null;
            replyToId = rm.getRepliedTo().getId();
        }

        //inserare in tabele
        String insertMsg = """
                INSERT INTO messages(from_id, message, data, reply_to_id)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        String insertTo = """
                INSERT INTO message_to(message_id, to_id)
                VALUES (?, ?)
                """;

        //pt tabela message
        Connection conn = null;
        try {
            conn = ConexiuneBD.getConnection();
            conn.setAutoCommit(false);

            Long newId;

            try (PreparedStatement ps = conn.prepareStatement(insertMsg)) {
                ps.setLong(1, m.getFrom().getId());
                ps.setString(2, m.getMessage());

                LocalDateTime dt = (m.getData() == null) ? LocalDateTime.now() : m.getData();
                ps.setTimestamp(3, Timestamp.valueOf(dt));

                if (replyToId == null) ps.setNull(4, Types.BIGINT);
                else ps.setLong(4, replyToId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Insert messages failed (no id returned).");
                    newId = rs.getLong("id");
                }
            }

            //pt tabela message_to
            try (PreparedStatement psTo = conn.prepareStatement(insertTo)) {
                for (User u : m.getTo()) {
                    psTo.setLong(1, newId);
                    psTo.setLong(2, u.getId());
                    psTo.addBatch();
                }
                psTo.executeBatch();
            }

            conn.commit();
            m.setId(newId);
            return m;

        } catch (SQLException e) {
            // daca ceva a picat anulam acel messaje
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return null;

        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean delete(Long id) {
        if (id == null) return false;

        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Mesaje trimise de user (outbox). */
    public List<Message> findByUserSent(User sender) {
        if (sender == null || sender.getId() == null) return List.of();

        String sql = """
                SELECT m.id, m.from_id, m.message, m.data, m.reply_to_id
                FROM messages m
                WHERE m.from_id = ?
                ORDER BY m.data
                """;

        return fetchMessagesWithTo(sql, ps -> ps.setLong(1, sender.getId()));
    }

    /** Mesaje primite de user = apare in message_to. */
    public List<Message> findByUserReceived(User receiver) {
        if (receiver == null || receiver.getId() == null) return List.of();

        String sql = """
                SELECT m.id, m.from_id, m.message, m.data, m.reply_to_id
                FROM messages m
                JOIN message_to mt ON mt.message_id = m.id
                WHERE mt.to_id = ?
                ORDER BY m.data
                """;

        return fetchMessagesWithTo(sql, ps -> ps.setLong(1, receiver.getId()));
    }

    /** Conversatia dintre A si B. */
    public List<Message> findConversation(User a, User b) {
        if (a == null || b == null) return List.of();
        if (a.getId() == null || b.getId() == null) return List.of();

        String sql = """
                SELECT DISTINCT m.id, m.from_id, m.message, m.data, m.reply_to_id
                FROM messages m
                JOIN message_to mt ON mt.message_id = m.id
                WHERE (m.from_id = ? AND mt.to_id = ?)
                   OR (m.from_id = ? AND mt.to_id = ?)
                ORDER BY m.data
                """;

        return fetchMessagesWithTo(sql, ps -> {
            ps.setLong(1, a.getId());
            ps.setLong(2, b.getId());
            ps.setLong(3, b.getId());
            ps.setLong(4, a.getId());
        });
    }


    //functia fetchMessagesWithTo nu stie ce parapetri trebuie sa pun in lista de mesaje
    @FunctionalInterface
    private interface StatementFiller { //seteaza parametri
        void fill(PreparedStatement ps) throws SQLException;
    }

    /** Citește mesaje din BD și le reconstruiește complet intr-o lista
     */
    private List<Message> fetchMessagesWithTo(String sql, StatementFiller filler) {
        List<Message> result = new ArrayList<>();

        //deschidem conexiunea
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            //setam parapetri
            filler.fill(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long msgId = rs.getLong("id");
                    Long fromId = rs.getLong("from_id");
                    String text = rs.getString("message");

                    Timestamp ts = rs.getTimestamp("data");
                    LocalDateTime dt = (ts == null) ? null : ts.toLocalDateTime();

                    Long replyToId = (Long) rs.getObject("reply_to_id");

                    User from = userRepository.findById(fromId); //cel care a trimis mesajul
                    List<User> to = loadToUsers(conn, msgId); //destinatarii

                    //construim obiectul
                    Message m;
                    if (replyToId != null) {
                        // safe: doar id-ul, restul null
                        Message repliedRef = new Message(replyToId, null, List.of(), null, null);
                        m = new ReplyMessage(msgId, from, to, text, dt, repliedRef);
                    } else {
                        m = new Message(msgId, from, to, text, dt);
                    }

                    result.add(m);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**lista de destinatari pt un message cu un id dat **/
    private List<User> loadToUsers(Connection conn, Long messageId) throws SQLException {
        String sql = """
                SELECT to_id
                FROM message_to
                WHERE message_id = ?
                ORDER BY to_id
                """;

        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long toId = rs.getLong("to_id");
                    User u = userRepository.findById(toId);
                    if (u != null) list.add(u);
                }
            }
        }
        return list;
    }
}
