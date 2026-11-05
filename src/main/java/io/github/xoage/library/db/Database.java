package io.github.xoage.library.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class Database implements AutoCloseable {

    private static final String CONFIG_FILE = "db.properties";

    private final HikariDataSource dataSource;             //пулл

    private Database(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

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
        HikariConfig config = new HikariConfig();     //куда подключаться
        config.setJdbcUrl(required(props, "db.url"));
        config.setUsername(required(props, "db.user"));
        config.setPassword(required(props, "db.password"));
        config.setMaximumPoolSize(intProperty(props, "db.pool.size", 5));
        config.setPoolName("library-pool");    // имя пула в логах

        return new Database(new HikariDataSource(config));
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("В " + CONFIG_FILE + " нет настройки " + key);
        }
        return value.trim();
    }

    private static int intProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;                       // ключа нет - берём значение по умолчанию
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number < 1) {
                throw new IllegalStateException(key + " должен быть больше 0, сейчас: " + number);
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " должен быть целым числом, сейчас: \"" + value + "\"", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();                // берём соединение
    }

    @Override
    public void close() {
        dataSource.close();                    // закрываем весь пул при выходе
    }

}
