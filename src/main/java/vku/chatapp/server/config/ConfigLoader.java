package vku.chatapp.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static ConfigLoader instance;

    private Properties serverProps;   // server.properties
    private Properties dbProps;       // database.properties
    private final Properties properties;

    private ConfigLoader() {
        serverProps = new Properties();
        dbProps = new Properties();
        properties = new Properties(); // <<< BẮT BUỘC CÓ
        loadProperties();
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigLoader.class) {
                if (instance == null) {
                    instance = new ConfigLoader();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        serverProps = new Properties();
        dbProps = new Properties();

        try (
                InputStream serverStream = getClass().getClassLoader()
                        .getResourceAsStream("config/server.properties");
                InputStream dbStream = getClass().getClassLoader()
                        .getResourceAsStream("config/database.properties")
        ) {

            if (serverStream != null) {
                serverProps.load(serverStream);
                System.out.println("✓ Loaded server.properties");
            } else {
                System.err.println("✗ server.properties NOT FOUND!");
            }

            if (dbStream != null) {
                dbProps.load(dbStream);
                System.out.println("✓ Loaded database.properties");
            } else {
                System.err.println("✗ database.properties NOT FOUND!");
            }

            properties.putAll(serverProps);
            properties.putAll(dbProps);

        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }

    // =============================
    // SERVER CONFIG
    // =============================
    public String getServerProperty(String key, String defaultValue) {
        return serverProps.getProperty(key, defaultValue);
    }

    public int getServerPropertyInt(String key, int defaultValue) {
        String value = serverProps.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // =============================
    // DATABASE CONFIG
    // =============================
    public String getDatabaseProperty(String key, String defaultValue) {
        return dbProps.getProperty(key, defaultValue);
    }

    public int getDatabasePropertyInt(String key, int defaultValue) {
        String value = dbProps.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Lấy property, trả về NULL nếu không có.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Lấy property, nếu không có trả về giá trị mặc định.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Kiểm tra property có tồn tại không.
     */
    public boolean contains(String key) {
        return properties.containsKey(key);
    }
}
