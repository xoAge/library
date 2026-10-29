package io.github.xoage.library;

import io.github.xoage.library.db.Database;
import io.github.xoage.library.db.DatabaseInitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

    public static void main(String[] args) {
        Database db = Database.fromConfig();
        System.out.println("База: " + db.url());
        try {
            new DatabaseInitializer(db).init();
            printBooks(db);
            printAuthors(db);
        } catch (SQLException e) {
            System.out.println("Ошибка базы данных: " + e.getMessage());
        }
    }

    private static void printBooks(Database db) throws SQLException {
        String sql = """  
                SELECT b.title, a.name AS author, b.year
                FROM books b
                JOIN authors a ON a.id = b.author_id
                ORDER BY a.name, b.year""";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println();
            System.out.println("Книги в библиотеке:");
            while (rs.next()) {
                System.out.printf("  %-26s %-18s %d%n",
                        rs.getString("title"), rs.getString("author"), rs.getInt("year"));
            }
        }
    }

    private static void printAuthors(Database db) throws SQLException {
        String sql = "SELECT name FROM authors ORDER BY name";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println();
            System.out.println("Авторы:");
            while (rs.next()) {
                System.out.println("  " + rs.getString("name"));
            }
        }
    }
}
