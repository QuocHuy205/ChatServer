package vku.chatapp.server.database.dao;

import vku.chatapp.common.model.Friend;
import vku.chatapp.common.enums.FriendRequestStatus;
import vku.chatapp.server.database.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public Friend createFriendRequest(Long userId, Long friendId) throws SQLException {
        // Check if friendship already exists (in either direction)
        String checkSql = """
            SELECT id, status FROM friends 
            WHERE (user_id = ? AND friend_id = ?) 
               OR (user_id = ? AND friend_id = ?)
        """;

        try (Connection conn = pool.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setLong(1, userId);
            checkStmt.setLong(2, friendId);
            checkStmt.setLong(3, friendId);
            checkStmt.setLong(4, userId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Friendship exists, return existing
                    Friend existing = new Friend();
                    existing.setId(rs.getLong("id"));
                    existing.setUserId(userId);
                    existing.setFriendId(friendId);
                    existing.setStatus(FriendRequestStatus.valueOf(rs.getString("status")));
                    return existing;
                }
            }
        }

        // Create new friend request
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, friendId);
            stmt.setString(3, FriendRequestStatus.ACCEPTED.name()); // Auto accept for simplicity

            stmt.executeUpdate();

            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            friend.setStatus(FriendRequestStatus.ACCEPTED);

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    friend.setId(keys.getLong(1));
                }
            }

            // Create reverse friendship for easy querying
            String reverseSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement reverseStmt = conn.prepareStatement(reverseSql)) {
                reverseStmt.setLong(1, friendId);
                reverseStmt.setLong(2, userId);
                reverseStmt.setString(3, FriendRequestStatus.ACCEPTED.name());
                reverseStmt.executeUpdate();
            }

            return friend;
        }
    }

    public boolean acceptFriendRequest(Long requestId) throws SQLException {
        String sql = "UPDATE friends SET status = ?, accepted_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, FriendRequestStatus.ACCEPTED.name());
            stmt.setLong(2, requestId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean rejectFriendRequest(Long requestId) throws SQLException {
        String sql = "UPDATE friends SET status = ? WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, FriendRequestStatus.REJECTED.name());
            stmt.setLong(2, requestId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeFriend(Long userId, Long friendId) throws SQLException {
        String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, friendId);
            stmt.setLong(3, friendId);
            stmt.setLong(4, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public List<Long> getFriendIds(Long userId) throws SQLException {
        String sql = """
            SELECT friend_id FROM friends 
            WHERE user_id = ? AND status = 'ACCEPTED'
        """;

        List<Long> friendIds = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friendIds.add(rs.getLong("friend_id"));
                }
            }
        }
        return friendIds;
    }

    public List<Friend> getPendingRequests(Long userId) throws SQLException {
        String sql = "SELECT * FROM friends WHERE friend_id = ? AND status = 'PENDING'";
        List<Friend> requests = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToFriend(rs));
                }
            }
        }
        return requests;
    }

    private Friend mapResultSetToFriend(ResultSet rs) throws SQLException {
        Friend friend = new Friend();
        friend.setId(rs.getLong("id"));
        friend.setUserId(rs.getLong("user_id"));
        friend.setFriendId(rs.getLong("friend_id"));
        friend.setStatus(FriendRequestStatus.valueOf(rs.getString("status")));

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) {
            friend.setRequestedAt(requestedAt.toLocalDateTime());
        }

        Timestamp acceptedAt = rs.getTimestamp("accepted_at");
        if (acceptedAt != null) {
            friend.setAcceptedAt(acceptedAt.toLocalDateTime());
        }

        return friend;
    }
}