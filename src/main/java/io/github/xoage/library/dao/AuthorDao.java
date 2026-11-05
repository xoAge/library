package io.github.xoage.library.dao;

import io.github.xoage.library.db.Database;
import io.github.xoage.library.model.Author;
import io.github.xoage.library.model.AuthorStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDao {

    private final Database db;

    public AuthorDao(Database db) {
        this.db = db;
    }
    ///Правило DAO: один класс отвечает за одну таблицу.
    public List<Author> findAll() throws SQLException {
        String sql = "select id, name from authors order by name";
        List<Author> authors = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                authors.add(new Author(rs.getInt("id"), rs.getString("name")));
            }
        }
        return authors;
    }
    public int add(Connection conn, String name) throws SQLException {
        String sql = "insert into authors (name) values (?) returning id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    /** Все авторы с количеством книг, включая авторов без книг. */
    public List<AuthorStats> findAllWithBookCount() throws SQLException {
        String sql = """
            select a.name, count(b.id) AS book_count
            from authors a
            left join books b on b.author_id = a.id
            group by a.id, a.name
            order by book_count desc, a.name
            """;
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            List<AuthorStats> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new AuthorStats(rs.getString("name"), rs.getInt("book_count")));
            }
            return result;
        }
    }
}
