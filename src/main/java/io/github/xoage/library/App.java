package io.github.xoage.library;

import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.db.Database;
import io.github.xoage.library.db.DatabaseInitializer;
import io.github.xoage.library.model.Author;
import io.github.xoage.library.model.Book;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class App {

    public static void main(String[] args) {
        Database db = Database.fromConfig();
        try {
            new DatabaseInitializer(db).init();

            BookDao bookDao = new BookDao(db);
            List<Book> books = bookDao.findAll();

            System.out.println("Книги в библиотеке (" + books.size() + "):");
            for (Book book : books) {
                System.out.printf("  %-26s %-18s %s%n",
                        book.title(), book.author().name(), book.year());
            }

            printAuthors(db);
        } catch (SQLException e) {
            System.out.println("Ошибка базы данных: " + e.getMessage());
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
