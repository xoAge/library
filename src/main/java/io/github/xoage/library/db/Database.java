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
        config.setMaximumPoolSize(5);          // не больше 5 соединений одновременно
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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();                // берём соединение
    }

    @Override
    public void close() {
        dataSource.close();                    // закрываем весь пул при выходе
    }

}
