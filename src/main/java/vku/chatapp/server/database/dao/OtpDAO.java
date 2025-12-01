package vku.chatapp.server.database.dao;

import vku.chatapp.server.database.ConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

public class OtpDAO {
    private final ConnectionPool pool = ConnectionPool.getInstance();
    private final Random random = new Random();

    public String generateOtp(String email, String purpose) throws SQLException {
        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        String sql = "INSERT INTO otp (email, otp_code, purpose, expires_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, otp);
            stmt.setString(3, purpose);
            stmt.setTimestamp(4, Timestamp.valueOf(expiresAt));

            int inserted = stmt.executeUpdate();

            if (inserted > 0) {
                System.out.println("========================================");
                System.out.println("OTP GENERATED FOR: " + email);
                System.out.println("PURPOSE: " + purpose);
                System.out.println("CODE: " + otp);
                System.out.println("EXPIRES AT: " + expiresAt);
                System.out.println("CURRENT TIME: " + LocalDateTime.now());
                System.out.println("========================================");
            }
        }

        return otp;
    }

    public boolean verifyOtp(String email, String otp, String purpose) throws SQLException {
        // Query với timestamp comparison
        String sql = """
            SELECT id, expires_at FROM otp 
            WHERE email = ? AND otp_code = ? AND purpose = ? 
            AND used = FALSE
            ORDER BY created_at DESC
            LIMIT 1
        """;

        System.out.println("========================================");
        System.out.println("VERIFYING OTP:");
        System.out.println("Email: " + email);
        System.out.println("Code: " + otp);
        System.out.println("Purpose: " + purpose);
        System.out.println("Current time: " + LocalDateTime.now());

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, otp);
            stmt.setString(3, purpose);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long otpId = rs.getLong("id");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    LocalDateTime expiryTime = expiresAt.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();

                    System.out.println("OTP Found!");
                    System.out.println("Expires at: " + expiryTime);
                    System.out.println("Current time: " + now);

                    // Manual expiry check
                    if (now.isBefore(expiryTime)) {
                        markOtpAsUsed(otpId);
                        System.out.println("OTP VERIFIED SUCCESSFULLY!");
                        System.out.println("========================================");
                        return true;
                    } else {
                        System.out.println("OTP EXPIRED!");
                        System.out.println("========================================");
                        return false;
                    }
                } else {
                    System.out.println("OTP NOT FOUND or already used");

                    // Debug: Check what OTPs exist
                    String debugSql = "SELECT otp_code, purpose, used, created_at, expires_at FROM otp WHERE email = ? ORDER BY created_at DESC LIMIT 5";
                    try (PreparedStatement debugStmt = conn.prepareStatement(debugSql)) {
                        debugStmt.setString(1, email);
                        try (ResultSet debugRs = debugStmt.executeQuery()) {
                            System.out.println("Recent OTPs for " + email + ":");
                            while (debugRs.next()) {
                                System.out.println("  Code: " + debugRs.getString("otp_code") +
                                        ", Purpose: " + debugRs.getString("purpose") +
                                        ", Used: " + debugRs.getBoolean("used") +
                                        ", Created: " + debugRs.getTimestamp("created_at") +
                                        ", Expires: " + debugRs.getTimestamp("expires_at"));
                            }
                        }
                    }
                    System.out.println("========================================");
                }
            }
        }
        return false;
    }

    private void markOtpAsUsed(long otpId) throws SQLException {
        String sql = "UPDATE otp SET used = TRUE WHERE id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, otpId);
            stmt.executeUpdate();
        }
    }
}