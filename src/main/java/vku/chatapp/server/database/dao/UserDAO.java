package vku.chatapp.server.database.dao;

import vku.chatapp.common.model.User;
import vku.chatapp.common.enums.UserStatus;
import vku.chatapp.server.database.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, display_name, email_verified) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getDisplayName());
            stmt.setBoolean(5, user.isEmailVerified());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }
        return user;
    }

    public User getUserById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public List<User> searchUsers(String query) throws SQLException {
        String sql = "SELECT * FROM users WHERE username LIKE ? OR display_name LIKE ? LIMIT 50";
        List<User> users = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    public boolean updateProfile(Long userId, String displayName, String bio, String avatarUrl) throws SQLException {
        String sql = "UPDATE users SET display_name = ?, bio = ?, avatar_url = ? WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, displayName);
            stmt.setString(2, bio);
            stmt.setString(3, avatarUrl);
            stmt.setLong(4, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Long userId, UserStatus status) throws SQLException {
        String sql = "UPDATE users SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(Long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean verifyEmail(Long userId) throws SQLException {
        String sql = "UPDATE users SET email_verified = TRUE WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setDisplayName(rs.getString("display_name"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setBio(rs.getString("bio"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            user.setStatus(UserStatus.valueOf(statusStr));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastSeen = rs.getTimestamp("last_seen");
        if (lastSeen != null) {
            user.setLastSeen(lastSeen.toLocalDateTime());
        }

        user.setEmailVerified(rs.getBoolean("email_verified"));
        return user;
    }
}
