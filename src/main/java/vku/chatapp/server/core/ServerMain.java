package vku.chatapp.server.core;

import vku.chatapp.server.database.DatabaseManager;
import vku.chatapp.server.config.ConfigLoader;

public class ServerMain {
    public static void main(String[] args) {
        try {
            System.out.println("=== VKU Chat Server ===");
            System.out.println("Starting server...");

            // Load configuration
            ConfigLoader config = ConfigLoader.getInstance();
            System.out.println("Configuration loaded");

            // Initialize database
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.initializeDatabase();
            System.out.println("Database initialized");

            // Start RMI server
            RMIServer rmiServer = new RMIServer();
            rmiServer.start();
            System.out.println("RMI Server started on port 1099");

            System.out.println("Server is ready to accept connections");
            System.out.println("Press Ctrl+C to stop the server");

            // Keep server running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}