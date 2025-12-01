package vku.chatapp.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static ConfigLoader instance;
    private Properties serverProps;
    private Properties dbProps;

    private ConfigLoader() {
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

        try (InputStream serverStream = getClass().getClassLoader()
                .getResourceAsStream("config/server.properties");
             InputStream dbStream = getClass().getClassLoader()
                     .getResourceAsStream("config/database.properties")) {

            if (serverStream != null) {
                serverProps.load(serverStream);
            }
            if (dbStream != null) {
                dbProps.load(dbStream);
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }

    public String getServerProperty(String key, String defaultValue) {
        return serverProps.getProperty(key, defaultValue);
    }

    public String getDatabaseProperty(String key, String defaultValue) {
        return dbProps.getProperty(key, defaultValue);
    }

    public int getServerPropertyInt(String key, int defaultValue) {
        String value = serverProps.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}