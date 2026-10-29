package io.github.xoage.library.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Готовит базу к работе: создаёт таблицы и, если база пустая, заполняет начальными данными. */
public final class DatabaseInitializer {

    private static final String SCHEMA = "db/schema.sql";
    private static final String DATA = "db/data.sql";

    private final Database db;

    public DatabaseInitializer(Database db) {
        this.db = db;
    }

    public void init() throws SQLException {
        try (Connection conn = db.getConnection()) {
            runScript(conn, SCHEMA);
            System.out.println("Схема готова (" + SCHEMA + ")");

            if (isEmpty(conn)) {
                conn.setAutoCommit(false);              // данные - всё или ничего
                try {
                    runScript(conn, DATA);
                    conn.commit();
                    System.out.println("Добавлены начальные данные (" + DATA + ")");
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } else {
                System.out.println("Данные уже есть, начальные данные не добавляем");
            }
        }
    }

    private static boolean isEmpty(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM authors")) {
            rs.next();
            return rs.getLong(1) == 0;
        }
    }

    private static void runScript(Connection conn, String resource) throws SQLException {
        String sql = readResource(resource);
        try (Statement st = conn.createStatement()) {
            st.execute(sql);                            // весь файл одним запросом
        }
    }

    private static String readResource(String resource) {
        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Не найден файл " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + resource, e);
        }
    }
}