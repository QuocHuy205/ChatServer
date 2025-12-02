// FILE: vku/chatapp/server/database/dao/CallHistoryDAO.java
// ✅ NEW: DAO để lưu call history vào database

package vku.chatapp.server.database.dao;

import vku.chatapp.common.model.CallHistory;
import vku.chatapp.common.enums.CallType;
import vku.chatapp.common.enums.CallStatus;
import vku.chatapp.server.database.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CallHistoryDAO {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public CallHistory createCall(CallHistory call) throws SQLException {
        String sql = """
            INSERT INTO call_history (caller_id, callee_id, call_type, status)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, call.getCallerId());
            stmt.setLong(2, call.getCalleeId());
            stmt.setString(3, call.getCallType().name());
            stmt.setString(4, call.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    call.setId(keys.getLong(1));
                }
            }

            System.out.println("✅ Call history created: ID=" + call.getId());
        }
        return call;
    }

    public boolean updateCallStatus(Long callId, CallStatus status) throws SQLException {
        String sql = "UPDATE call_history SET status = ? WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, callId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean startCall(Long callId) throws SQLException {
        String sql = """
            UPDATE call_history 
            SET status = 'CONNECTED', started_at = CURRENT_TIMESTAMP 
            WHERE id = ?
        """;

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, callId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean endCall(Long callId, int durationSeconds) throws SQLException {
        String sql = """
            UPDATE call_history 
            SET status = 'ENDED', 
                ended_at = CURRENT_TIMESTAMP,
                duration_seconds = ?
            WHERE id = ?
        """;

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, durationSeconds);
            stmt.setLong(2, callId);

            return stmt.executeUpdate() > 0;
        }
    }

    public List<CallHistory> getCallHistory(Long userId, int limit) throws SQLException {
        String sql = """
            SELECT * FROM call_history
            WHERE caller_id = ? OR callee_id = ?
            ORDER BY created_at DESC
            LIMIT ?
        """;

        List<CallHistory> calls = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    calls.add(mapResultSetToCallHistory(rs));
                }
            }
        }
        return calls;
    }

    private CallHistory mapResultSetToCallHistory(ResultSet rs) throws SQLException {
        CallHistory call = new CallHistory();
        call.setId(rs.getLong("id"));
        call.setCallerId(rs.getLong("caller_id"));
        call.setCalleeId(rs.getLong("callee_id"));
        call.setCallType(CallType.valueOf(rs.getString("call_type")));
        call.setStatus(CallStatus.valueOf(rs.getString("status")));

        Integer duration = rs.getInt("duration_seconds");
        if (!rs.wasNull()) {
            call.setDurationSeconds(duration);
        }

        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) {
            call.setStartedAt(startedAt.toLocalDateTime());
        }

        Timestamp endedAt = rs.getTimestamp("ended_at");
        if (endedAt != null) {
            call.setEndedAt(endedAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            call.setCreatedAt(createdAt.toLocalDateTime());
        }

        return call;
    }
}