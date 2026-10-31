package io.github.xoage.library.dao;

import io.github.xoage.library.db.Database;
import io.github.xoage.library.model.Author;
import io.github.xoage.library.model.Book;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private static final String SELECT_BOOKS = """
            select b.id, b.title, b.year, b.isbn,
                   a.id as author_id,
                   a.name AS author_name
            from books b
            join authors a on a.id = b.author_id
            """;

    private final Database db;

    public BookDao(Database db) {
        this.db = db;
    }

    /** Все книги, по автору и году. */
    public List<Book> findAll() throws SQLException {
        String sql = SELECT_BOOKS + " ORDER BY a.name, b.year";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                books.add(mapRow(rs)); //«переводчик» одной строки ответа в объект.
            }
            return books;
        }
    }

    /** Одна строка ответа - один объект Book. */
    private static Book mapRow(ResultSet rs) throws SQLException {
        Author author = new Author(
                rs.getInt("author_id"),
                rs.getString("author_name"));
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                author,
                rs.getObject("year", Integer.class),
                rs.getString("isbn"));
    }
}
