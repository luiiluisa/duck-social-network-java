package repository.BDrepo;

import model.Friendship;
import model.User;
import repository.ConexiuneBD;
import repository.FriendshipRepository;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendshipBDRepository extends FriendshipRepository {

    private final UserRepository userRepository;

    public FriendshipBDRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**putem id in ordine crescatoare**/
    private long[] normalOrder(User a, User b) {
        long id1 = a.getId();
        long id2 = b.getId();
        if (id1 < id2)
            return new long[]{id1, id2};
        else
            return new long[]{id2, id1};
    }

    /**Creeaza prietenii*/
    @Override
    public boolean addFriendship(User a, User b) {
        if (a == null || b == null) return false;

        long[] ids = normalOrder(a, b); //ia id userilor

        String sql = "INSERT INTO friendships(user_id_1, user_id_2) VALUES (?, ?)";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ids[0]);
            ps.setLong(2, ids[1]);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    /**Eliminam pritenii*/
    @Override
    public boolean removeFriendship(User a, User b) {
        long[] ids = normalOrder(a, b);
        String sql = "DELETE FROM friendships WHERE user_id_1 = ? AND user_id_2 = ?";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ids[0]);
            ps.setLong(2, ids[1]);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**Stergerem toate priteniile de la user-ul u*/
    @Override
    public int removeAllForUser(User u) {
        if (u == null) return 0;

        String sql = "DELETE FROM friendships WHERE user_id_1 = ? OR user_id_2 = ?";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, u.getId());
            ps.setLong(2, u.getId());

            return ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**Citeste si transforma prieteniile intr-un set de obiecte*/
    @Override
    public Set<Friendship> findAllEdges() {
        Set<Friendship> result = new HashSet<>();

        String sql = "SELECT user_id_1, user_id_2 FROM friendships";

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> allUsers = userRepository.findAll();

            Map<Long, User> byId = allUsers.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            while (rs.next()) {
                long id1 = rs.getLong("user_id_1");
                long id2 = rs.getLong("user_id_2");

                User a = byId.get(id1);
                User b = byId.get(id2);

                if (a != null && b != null) {
                    result.add(new Friendship(a, b));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
