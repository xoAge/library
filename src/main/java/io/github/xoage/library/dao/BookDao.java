package io.github.xoage.library.dao;

import io.github.xoage.library.db.Database;
import io.github.xoage.library.model.Author;
import io.github.xoage.library.model.Book;

import java.sql.*;
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
        String sql = SELECT_BOOKS + " order by a.name, b.year";
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

    /** Книги, в названии которых есть часть part (без учёта регистра). */
    public List<Book> findByTitle(String part) throws SQLException {
        String sql = SELECT_BOOKS + " where b.title ilike ? order by b.title"; // ? шаблон с окошками значений, ковычки тоже значение
                                                                                //ilike не учитываем регистр
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {  //вместо createStatement(): создаём «бланк» с окошками.

            String escaped = part.replace("\\", "\\\\")
                    .replace("%", "\\%").replace("_", "\\_");
            ps.setString(1, "%" + escaped + "%"); //заполняем окошко номер 1, любые символы до и после part

            try (ResultSet rs = ps.executeQuery()) { //Внутренний try закроет ResultSet, внешний закроет ps и conn.
                                                     // ResultSet появляется только после ps.setString() и executeQuery()
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
                return books;
            }
        }
    }
}
