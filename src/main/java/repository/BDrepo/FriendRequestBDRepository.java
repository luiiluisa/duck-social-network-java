package repository.BDrepo;

import model.FriendRequest;
import model.Enums.FriendRequestStatus;
import model.User;
import repository.ConexiuneBD;
import repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestBDRepository {

    private final UserRepository userRepository;

    public FriendRequestBDRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public FriendRequest saveRequest(Long fromId, Long toId) {
        if (fromId == null || toId == null) return null;
        if (fromId.equals(toId)) return null;

        String sql = """
                INSERT INTO friendship_requests(from_id, to_id, status)
                VALUES (?, ?, 'PENDING')
                RETURNING id, created_at
                """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, fromId);
            ps.setLong(2, toId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                long id = rs.getLong("id");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime dt = ts == null ? null : ts.toLocalDateTime();

                User from = userRepository.findById(fromId);
                User to = userRepository.findById(toId);
                if (from == null || to == null) return null;

                return new FriendRequest(id, from, to, FriendRequestStatus.PENDING, dt);
            }

        } catch (SQLException e) {
            // foarte des aici o sa prinzi "duplicate key" din unique index -> cerere deja exista
            return null;
        }
    }

    public List<FriendRequest> findPendingForUser(Long toId) {
        if (toId == null) return List.of();

        String sql = """
                SELECT id, from_id, to_id, status, created_at
                FROM friendship_requests
                WHERE to_id = ? AND status = 'PENDING'
                ORDER BY created_at DESC
                """;

        List<FriendRequest> out = new ArrayList<>();
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, toId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    long fromId = rs.getLong("from_id");
                    long tId = rs.getLong("to_id");
                    String st = rs.getString("status");
                    Timestamp ts = rs.getTimestamp("created_at");

                    User from = userRepository.findById(fromId);
                    User to = userRepository.findById(tId);
                    if (from == null || to == null) continue;

                    FriendRequestStatus status = FriendRequestStatus.valueOf(st);
                    LocalDateTime dt = ts == null ? null : ts.toLocalDateTime();

                    out.add(new FriendRequest(id, from, to, status, dt));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    public FriendRequest findById(Long requestId) {
        if (requestId == null) return null;

        String sql = """
                SELECT id, from_id, to_id, status, created_at
                FROM friendship_requests
                WHERE id = ?
                """;

        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                long id = rs.getLong("id");
                long fromId = rs.getLong("from_id");
                long toId = rs.getLong("to_id");
                String st = rs.getString("status");
                Timestamp ts = rs.getTimestamp("created_at");

                User from = userRepository.findById(fromId);
                User to = userRepository.findById(toId);
                if (from == null || to == null) return null;

                FriendRequestStatus status = FriendRequestStatus.valueOf(st);
                LocalDateTime dt = ts == null ? null : ts.toLocalDateTime();

                return new FriendRequest(id, from, to, status, dt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateStatus(Long requestId, FriendRequestStatus status) {
        if (requestId == null || status == null) return false;

        String sql = "UPDATE friendship_requests SET status = ? WHERE id = ?";
        try (Connection conn = ConexiuneBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, requestId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
