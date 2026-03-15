package repository.BDrepo;

import model.Event;
import model.User;
import model.observer.Observer;
import repository.ConexiuneBD;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pentru Event, care foloseste:
 *   events              (lista evenimentelor)
 *   event_subscribers   (tabela de legatura event - user)
 */
public class EventBDRepository {

    private final UserRepository userRepository;

    /**
     * Repository-ul are nevoie de UserRepository pentru a putea reconstrui
     * obiectele User atunci cand citim abonatii unui eveniment
     */
    public EventBDRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Salveaza un eveniment in BD
     */
    public boolean save(Event e) {
        if (e == null) return false;

        if (e.getId() == null) {
            String sql = "INSERT INTO events DEFAULT VALUES RETURNING id";
            try (Connection conn = ConexiuneBD.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    long id = rs.getLong("id");
                    e.setId(id);
                    return true;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
            return false;
        }
        return true;
    }

    /**
     * Sterge un eveniment din BD.
     * Sterge mai intai toti abonatii din event_subscribers, apoi randul din events*/
    public boolean delete(Event e) {
        if (e == null || e.getId() == null) return false;

        String sqlSubs = "DELETE FROM event_subscribers WHERE event_id = ?";
        String sqlEvt  = "DELETE FROM events WHERE id = ?";

        try (Connection conn = ConexiuneBD.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlSubs);
                 PreparedStatement ps2 = conn.prepareStatement(sqlEvt)) {

                // stergem abonatii
                ps1.setLong(1, e.getId());
                ps1.executeUpdate();

                // stergem evenimentul propriu-zis
                ps2.setLong(1, e.getId());
                int rows = ps2.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Adauga un user ca abonat al evenimentului,
     * inserand o pereche (event_id, user_id) in event_subscribers.
     */
    public boolean subscribe(Event e, User u) {
        if (e == null || u == null || e.getId() == null || u.getId() == null) return false;

        String sql = """
                INSERT INTO event_subscribers(event_id, user_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING 
                """;//nu putem abona un user de doua ori la acelasi event
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, e.getId());
            ps.setLong(2, u.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Dezaboneaza un user de la un eveniment*/
    public boolean unsubscribe(Event e, User u) {
        if (e == null || u == null || e.getId() == null || u.getId() == null) return false;

        String sql = "DELETE FROM event_subscribers WHERE event_id = ? AND user_id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, e.getId());
            ps.setLong(2, u.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Intoarce toate evenimentele existente in tabela events*/
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT id FROM events";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                Event e = new Event();
                e.setId(id);
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    /**
     * Citeste din BD toti abonatii unui eveniment
     * ii transforma in obiecte User si intoarce o lista de Observer.
     * Aceasta lista este folosita de EventService la notifyAll.
     */
    public List<Observer> listSubscribers(Event e) {
        List<Observer> result = new ArrayList<>();
        if (e == null || e.getId() == null) return result;

        String sql = "SELECT user_id FROM event_subscribers WHERE event_id = ?";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, e.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long userId = rs.getLong("user_id");
                    User u = findUserById(userId);
                    if (u != null) result.add(u); // User implementeaza Observer
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    /**
     * Cauta un utilizator dupa id folosind UserRepository,
     * parcurgand lista de useri din memorie*/
    private User findUserById(Long id) {
        for (User u : userRepository.findAll()) {
            if (id.equals(u.getId())) return u;
        }
        return null;
    }

    public List<Event> findEventsForUser(Long userId) {
        List<Event> list = new ArrayList<>();
        if (userId == null) return list;

        String sql = """
        SELECT e.id
        FROM events e
        JOIN event_subscribers es ON es.event_id = e.id
        WHERE es.user_id = ?
        ORDER BY e.id
    """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getLong("id"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public List<Event> findSubscribedEvents(Long userId) {
        List<Event> list = new ArrayList<>();
        if (userId == null) return list;

        String sql = """
            SELECT e.id
            FROM events e
            JOIN event_subscribers es ON es.event_id = e.id
            WHERE es.user_id = ?
            ORDER BY e.id
            """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getLong("id"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean isUserSubscribed(Long eventId, Long userId) {
        if (eventId == null || userId == null) return false;

        String sql = "SELECT 1 FROM event_subscribers WHERE event_id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
