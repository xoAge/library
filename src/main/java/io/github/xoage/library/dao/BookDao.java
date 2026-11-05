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

    /** Текст пользователя → шаблон для ILIKE: %часть% с экранированными \ % _ */
    private static String likePattern(String part) {
        String escaped = part.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /** Выполнить запрос с одним параметром-шаблоном и собрать книги. */
    private List<Book> findByPattern(String sql, String part) throws SQLException {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, likePattern(part));
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
                return books;
            }
        }
    }

    /** Книги, в названии которых есть part (без учёта регистра). */
    public List<Book> findByTitle(String part) throws SQLException {
        return findByPattern(SELECT_BOOKS + " where b.title ilike ? order by b.title", part);
    }

    /** Книги авторов, в имени которых есть part (без учёта регистра). */
    public List<Book> findByAuthor(String part) throws SQLException {
        return findByPattern(SELECT_BOOKS + " where a.name ilike ? order by a.name, b.year", part);
    }

    public int add(String title, int authorId, Integer year, String isbn) throws SQLException { //одна транзакция, соединение закрывает тот, кто открыл
        try (Connection conn = db.getConnection()) {
            return add(conn, title, authorId, year, isbn);
        }
    }

    public int add(Connection conn, String title, int authorId, Integer year, String isbn) throws SQLException {
        String sql = """
            insert into books (title, author_id, year, isbn)
            values (?, ?, ?, ?)
            returning id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, authorId);
            ps.setObject(3, year, Types.INTEGER);
            ps.setString(4, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?"; //WHERE id = ? - условие отбора строк: «удалить только те строки, у которых id равен вот этому значению».
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    public boolean update(int id, String newTitle, Integer newYear) throws SQLException {
        //пользователь ввёл название в ? попадает «Дни в Бирме», COALESCE берёт его;
        //пользователь нажал Enter в ? попадает NULL, COALESCE берёт второе, то есть текущее title из этой же строки
        String sql = """ 
            update books
            set title = coalesce(?, title),
                year  = coalesce(?, year)
            where id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newTitle);
            ps.setObject(2, newYear, Types.INTEGER);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }
}
