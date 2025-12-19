// FILE: vku/chatapp/server/database/dao/MessageDAO.java
// ✅ FIX: SQL syntax error trong updateMessageStatus

package vku.chatapp.server.database.dao;

import vku.chatapp.common.model.Message;
import vku.chatapp.common.enums.MessageType;
import vku.chatapp.common.enums.MessageStatus;
import vku.chatapp.server.database.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDAO {
    private final ConnectionPool pool = ConnectionPool.getInstance();
    private static final long DUPLICATE_EXPIRE_MS = 3000; // 3 giây
    private static final ConcurrentHashMap<String, Long> MESSAGE_CACHE = new ConcurrentHashMap<>();

    public Message saveMessage(Message message) throws SQLException {

        // 🔒 CHỐNG GỬI TRÙNG TẠI DAO (KHÔNG ĐỤNG MYSQL)
        if (isDuplicateMessage(message)) {
            System.out.println("⚠️ Duplicate message blocked by DAO");
            return message; // Không INSERT
        }

        String sql = """
        INSERT INTO messages (sender_id, receiver_id, group_id, content, type, status,
                             file_url, file_name, file_size)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, message.getSenderId());

            if (message.getReceiverId() != null) {
                stmt.setLong(2, message.getReceiverId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            if (message.getGroupId() != null) {
                stmt.setLong(3, message.getGroupId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, message.getContent());
            stmt.setString(5, message.getType().name());
            stmt.setString(6, message.getStatus().name());
            stmt.setString(7, message.getFileUrl());
            stmt.setString(8, message.getFileName());

            if (message.getFileSize() != null) {
                stmt.setLong(9, message.getFileSize());
            } else {
                stmt.setNull(9, Types.BIGINT);
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    message.setId(keys.getLong(1));
                }
            }

            System.out.println("✅ Message saved to DB: ID=" + message.getId());
        }

        return message;
    }


    public List<Message> getConversationHistory(Long user1Id, Long user2Id, int limit) throws SQLException {
        String sql = """
            SELECT * FROM messages 
            WHERE (sender_id = ? AND receiver_id = ?) 
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY sent_at DESC
            LIMIT ?
        """;

        List<Message> messages = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, user1Id);
            stmt.setLong(2, user2Id);
            stmt.setLong(3, user2Id);
            stmt.setLong(4, user1Id);
            stmt.setInt(5, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }

        System.out.println("✅ Loaded " + messages.size() + " messages from DB for conversation: " +
                user1Id + " <-> " + user2Id);

        return messages;
    }

    // ✅ FIX: SQL syntax error - thiếu SET clause
    public boolean updateMessageStatus(Long messageId, MessageStatus status) throws SQLException {
        String sql;

        // Build SQL based on status
        switch (status) {
            case DELIVERED:
                sql = "UPDATE messages SET status = ?, delivered_at = CURRENT_TIMESTAMP WHERE id = ?";
                break;
            case READ:
                sql = "UPDATE messages SET status = ?, read_at = CURRENT_TIMESTAMP WHERE id = ?";
                break;
            default:
                sql = "UPDATE messages SET status = ? WHERE id = ?";
        }

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, messageId);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                System.out.println("✅ Message status updated: ID=" + messageId + ", Status=" + status);
                return true;
            } else {
                System.err.println("⚠️ No message found with ID: " + messageId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating message status: " + e.getMessage());
            throw e;
        }
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setSenderId(rs.getLong("sender_id"));

        long receiverId = rs.getLong("receiver_id");
        if (!rs.wasNull()) {
            message.setReceiverId(receiverId);
        }

        long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            message.setGroupId(groupId);
        }

        message.setContent(rs.getString("content"));
        message.setType(MessageType.valueOf(rs.getString("type")));
        message.setStatus(MessageStatus.valueOf(rs.getString("status")));
        message.setFileUrl(rs.getString("file_url"));
        message.setFileName(rs.getString("file_name"));

        long fileSize = rs.getLong("file_size");
        if (!rs.wasNull()) {
            message.setFileSize(fileSize);
        }

        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) {
            message.setSentAt(sentAt.toLocalDateTime());
        }

        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            message.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            message.setReadAt(readAt.toLocalDateTime());
        }

        return message;
    }

    /**
     * Kiểm tra message có bị gửi trùng trong thời gian ngắn hay không
     * @return true nếu là message trùng cần chặn
     */
    private boolean isDuplicateMessage(Message message) {

        String fingerprint =
                message.getSenderId() + ":" +
                        message.getReceiverId() + ":" +
                        message.getType() + ":" +
                        (message.getContent() != null
                                ? message.getContent().hashCode()
                                : 0);

        long now = System.currentTimeMillis();
        Long lastTime = MESSAGE_CACHE.get(fingerprint);

        // Nếu tồn tại và gửi lại quá nhanh → duplicate
        if (lastTime != null && (now - lastTime) < DUPLICATE_EXPIRE_MS) {
            return true;
        }

        // Lưu lại dấu vết message này
        MESSAGE_CACHE.put(fingerprint, now);
        return false;
    }

}