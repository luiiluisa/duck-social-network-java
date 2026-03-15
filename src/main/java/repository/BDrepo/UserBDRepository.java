package repository.BDrepo;

import model.*;
import model.Enums.TipDuck;
import repository.ConexiuneBD;
import repository.UserRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pentru user din Baza de date
 */
public class UserBDRepository extends UserRepository {

    // transforma un rand din tabela => obiect User
    private User mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String userType = rs.getString("user_type");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");

        if ("PERSON".equalsIgnoreCase(userType)) {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            Date bd = rs.getDate("birth_date");
            LocalDate dataN = (bd == null) ? null : bd.toLocalDate();
            String ocupatie = rs.getString("occupation");
            double emp = rs.getDouble("empathy");

            return new Person(id, username, email, password,
                    firstName, lastName, dataN, ocupatie, emp);
        } else { // DUCK
            String duckKind = rs.getString("duck_kind");
            TipDuck tip = (duckKind == null || duckKind.isBlank())
                    ? TipDuck.SWIMMING
                    : TipDuck.valueOf(duckKind.trim()); // IMPORTANT: trim()

            double viteza = rs.getDouble("speed");
            double rezistenta = rs.getDouble("stamina");

            if (tip == TipDuck.FLYING) {
                return new FlyingDuck(id, username, email, password, viteza, rezistenta);
            } else {
                return new SwimmingDuck(id, username, email, password, viteza, rezistenta);
            }
        }
    }

    /**
     * Salveaza un user nou in BD.
     * ID-ul e BIGSERIAL -> generat de BD.
     * IMPORTANT: parola se salveaza criptata (salt:hash)
     */
    @Override
    public User save(User user) {
        if (user == null) return null;

        String insertSql = """
                INSERT INTO users(
                    user_type, username, email, password,
                    first_name, last_name, birth_date, occupation, empathy,
                    duck_kind, speed, stamina
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            // parola criptata pentru ORICE tip de user
            String storedPassword = security.PasswordUtils.makeStoredPassword(user.getPassword());

            if (user instanceof Person p) {
                ps.setString(1, "PERSON");
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getEmail());
                ps.setString(4, storedPassword);

                ps.setString(5, p.getNume());       // first_name
                ps.setString(6, p.getPrenume());    // last_name

                LocalDate dn = p.getDataNasterii();
                if (dn != null) ps.setDate(7, Date.valueOf(dn));
                else ps.setNull(7, Types.DATE);

                ps.setString(8, p.getOcupatie());
                ps.setDouble(9, p.getNivelEmpatie());

                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.DOUBLE);
                ps.setNull(12, Types.DOUBLE);

            } else if (user instanceof Duck d) {
                ps.setString(1, "DUCK");
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getEmail());
                ps.setString(4, storedPassword);

                // campuri Person = NULL
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.DATE);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.DOUBLE);

                TipDuck tip = d.getTip();
                ps.setString(10, tip == null ? null : tip.name());
                ps.setDouble(11, d.getViteza());
                ps.setDouble(12, d.getRezistenta());

            } else {
                return null;
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long newId = rs.getLong("id");
                    user.setId(newId);
                }
            }

            // tinem consistent cu BD
            user.setPassword(storedPassword);

            return user;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update user existent.
     * IMPORTANT:
     * - daca parola din obiect e goala/null => pastreaza parola din BD
     * - altfel => cripteaza parola noua si o salveaza
     */
    @Override
    public User update(User user) {
        if (user == null || user.getId() == null) return null;

        // luam parola existenta din BD ca fallback
        String oldPassword = null;
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT password FROM users WHERE id = ?")) {
            ps.setLong(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) oldPassword = rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        if (oldPassword == null) return null;

        // daca UI nu a dat parola noua, pastram parola veche
        String raw = user.getPassword();
        String storedPassword = (raw == null || raw.isBlank())
                ? oldPassword
                : security.PasswordUtils.makeStoredPassword(raw);

        String sql = """
                UPDATE users SET
                    user_type = ?, username = ?, email = ?, password = ?,
                    first_name = ?, last_name = ?, birth_date = ?, occupation = ?, empathy = ?,
                    duck_kind = ?, speed = ?, stamina = ?
                WHERE id = ?
                """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (user instanceof Person p) {
                ps.setString(1, "PERSON");
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getEmail());
                ps.setString(4, storedPassword);

                ps.setString(5, p.getNume());
                ps.setString(6, p.getPrenume());

                LocalDate dn = p.getDataNasterii();
                if (dn != null) ps.setDate(7, Date.valueOf(dn));
                else ps.setNull(7, Types.DATE);

                ps.setString(8, p.getOcupatie());
                ps.setDouble(9, p.getNivelEmpatie());

                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.DOUBLE);
                ps.setNull(12, Types.DOUBLE);

            } else if (user instanceof Duck d) {
                ps.setString(1, "DUCK");
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getEmail());
                ps.setString(4, storedPassword);

                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.DATE);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.DOUBLE);

                TipDuck tip = d.getTip();
                ps.setString(10, tip == null ? null : tip.name());
                ps.setDouble(11, d.getViteza());
                ps.setDouble(12, d.getRezistenta());

            } else {
                return null;
            }

            ps.setLong(13, user.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                user.setPassword(storedPassword); // consistent cu BD
                return user;
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Sterge un User */
    @Override
    public boolean delete(Long id) {
        if (id == null) return false;

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Ia toti userii */
    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /** Cauta un user dupa ID */
    @Override
    public User findById(Long id) {
        if (id == null) return null;

        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Paginare pentru rate, direct din BD.
     * page incepe de la 0
     */
    public List<Duck> findDucksPage(int page, int pageSize) {
        List<Duck> list = new ArrayList<>();
        if (page < 0 || pageSize <= 0) return list;

        int offset = page * pageSize;

        String sql = """
                SELECT *
                FROM users
                WHERE user_type = 'DUCK'
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = mapRow(rs);
                    if (u instanceof Duck d) list.add(d);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /** Cate rate sunt */
    public int countDucks() {
        String sql = "SELECT COUNT(*) AS cnt FROM users WHERE user_type = 'DUCK'";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("cnt");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
