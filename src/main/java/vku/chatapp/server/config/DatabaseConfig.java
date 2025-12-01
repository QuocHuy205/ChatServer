package vku.chatapp.server.config;

public class DatabaseConfig {
    private String url;
    private String username;
    private String password;
    private int maxPoolSize;
    private int minIdle;
    private long connectionTimeout;

    public DatabaseConfig() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.url = config.getDatabaseProperty("db.url", "jdbc:mysql://localhost:3306/chatapp");
        this.username = config.getDatabaseProperty("db.username", "root");
        this.password = config.getDatabaseProperty("db.password", "");
        this.maxPoolSize = Integer.parseInt(config.getDatabaseProperty("db.pool.maxSize", "10"));
        this.minIdle = Integer.parseInt(config.getDatabaseProperty("db.pool.minIdle", "2"));
        this.connectionTimeout = Long.parseLong(config.getDatabaseProperty("db.pool.timeout", "30000"));
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getMinIdle() { return minIdle; }
    public long getConnectionTimeout() { return connectionTimeout; }
}
