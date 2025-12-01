package vku.chatapp.server.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseManager {
    private ConnectionPool connectionPool;

    public DatabaseManager() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    public void initializeDatabase() {
        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    display_name VARCHAR(100),
                    avatar_url VARCHAR(500),
                    bio TEXT,
                    status VARCHAR(20) DEFAULT 'OFFLINE',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_seen TIMESTAMP,
                    email_verified BOOLEAN DEFAULT FALSE
                )
            """);

            // Friends table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS friends (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    friend_id BIGINT NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    accepted_at TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (friend_id) REFERENCES users(id)
                )
            """);

            // Messages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    sender_id BIGINT NOT NULL,
                    receiver_id BIGINT,
                    group_id BIGINT,
                    content TEXT,
                    type VARCHAR(20) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    file_url VARCHAR(500),
                    file_name VARCHAR(255),
                    file_size BIGINT,
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    delivered_at TIMESTAMP,
                    read_at TIMESTAMP,
                    FOREIGN KEY (sender_id) REFERENCES users(id)
                )
            """);

            // Groups table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS groups (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    avatar_url VARCHAR(500),
                    creator_id BIGINT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (creator_id) REFERENCES users(id)
                )
            """);

            // Group Members table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS group_members (
                    group_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (group_id, user_id),
                    FOREIGN KEY (group_id) REFERENCES groups(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // OTP table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS otp (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    email VARCHAR(100) NOT NULL,
                    otp_code VARCHAR(10) NOT NULL,
                    purpose VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NOT NULL,
                    used BOOLEAN DEFAULT FALSE
                )
            """);

            System.out.println("Database initialized successfully");

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}