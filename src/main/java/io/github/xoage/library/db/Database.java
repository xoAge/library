package io.github.xoage.library.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {

    private static final String CONFIG_FILE = "db.properties";

    private final String url;
    private final String user;
    private final String password;

    private Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Читает настройки из db.properties (лежит в src/main/resources). */
    public static Database fromConfig() {
        Properties props = new Properties();
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Не найден файл " + CONFIG_FILE
                        + ". Скопируй db.properties.example в db.properties и впиши свои данные.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + CONFIG_FILE, e);
        }
        return new Database(
                required(props, "db.url"),
                required(props, "db.user"),
                required(props, "db.password"));
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("В " + CONFIG_FILE + " нет настройки " + key);
        }
        return value.trim();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public String url() {
        return url;
    }
}
