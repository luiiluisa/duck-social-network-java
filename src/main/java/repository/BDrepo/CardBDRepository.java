package repository.BDrepo;

import model.Card;
import model.Duck;
import model.Enums.TipDuck;
import model.FlyingDuck;
import model.SwimmingDuck;
import repository.ConexiuneBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardBDRepository {

    /**Adaugam un card in baza de date*/
    public Card save(Card c) {
        if (c == null) return null;

        try (Connection conn = ConexiuneBD.getConnection()) {
            if (c.getId() == null) {
                String sql = "INSERT INTO card(nume_card) VALUES (?)";
                try (PreparedStatement ps =
                             conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, c.getNumeCard());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            c.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE card SET nume_card = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, c.getNumeCard());
                    ps.setLong(2, c.getId());
                    ps.executeUpdate();
                }
            }
            return c;
        } catch (SQLException e) {
            return null;
        }
    }

    /**stergem un card */
    public boolean delete(Long id) {
        if (id == null) return false;
        String sql = "DELETE FROM card WHERE id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**cautam un card*/
    public Card findById(Long id) {
        if (id == null) return null;
        String sql = "SELECT id, nume_card FROM card WHERE id = ?";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Card c = new Card(rs.getString("nume_card"));
                    c.setId(rs.getLong("id"));
                    loadMembers(conn, c);
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**ne da toate listele cu membri din toate card din BD*/
    public List<Card> findAll() {
        List<Card> result = new ArrayList<>();
        String sql = "SELECT id, nume_card FROM card";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Card c = new Card(rs.getString("nume_card"));
                c.setId(rs.getLong("id"));
                // pentru fiecare card adaugam membrii lui in obiectul card
                loadMembers(conn, c);
                result.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**pentru un card=>lista de duck din acel duck*/
    private void loadMembers(Connection conn, Card c) throws SQLException {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN card_members cm ON cm.duck_id = u.id
                WHERE cm.card_id = ? AND u.user_type = 'DUCK'
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    c.addDuck(mapRowToDuck(rs));
                }
            }
        }
    }

    /**transforma un duck inr-un obiect duck (de tip swimming sau flying)*/
    private Duck mapRowToDuck(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String kind = rs.getString("duck_kind");
        double speed = rs.getDouble("speed");
        double stamina = rs.getDouble("stamina");

        TipDuck tip = TipDuck.valueOf(kind);
        if (tip == TipDuck.FLYING) {
            return new FlyingDuck(id, username, email, password, speed, stamina);
        } else {
            return new SwimmingDuck(id, username, email, password, speed, stamina);
        }
    }

    /**adauga o rata intr-un card*/
    public boolean addDuckToCard(Long cardId, Duck d) {
        if (cardId == null || d == null || d.getId() == null) return false;

        String sql = "INSERT INTO card_members(card_id, duck_id) VALUES (?, ?) ON CONFLICT DO NOTHING"; //fara duplicate
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cardId);
            ps.setLong(2, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**sterge o rata dintr-un card*/
    public boolean removeDuckFromCard(Long cardId, Duck d) {
        if (cardId == null || d == null || d.getId() == null) return false;

        String sql = "DELETE FROM card_members WHERE card_id = ? AND duck_id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cardId);
            ps.setLong(2, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**sterge o rata din toate cardurile in care este*/
    public int removeDuckEverywhereById(Long duckId) {
        if (duckId == null) return 0;

        String sql = "DELETE FROM card_members WHERE duck_id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, duckId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
