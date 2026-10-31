package io.github.xoage.library.dao;

import io.github.xoage.library.db.Database;
import io.github.xoage.library.model.Author;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
