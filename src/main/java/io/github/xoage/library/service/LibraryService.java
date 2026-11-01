package io.github.xoage.library.service;

import io.github.xoage.library.dao.AuthorDao;
import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.db.Database;

import java.sql.Connection;
import java.sql.SQLException;
///третий слой - сервис. Он собирает из нескольких операций DAO одно действие, осмысленное для пользователя.
/// Так устроены почти все Java-приложения, и в Spring будет то же самое (@Service).
public class LibraryService {

    private final Database db;
    private final AuthorDao authorDao;
    private final BookDao bookDao;

    public LibraryService(Database db, AuthorDao authorDao, BookDao bookDao) {
        this.db = db;
        this.authorDao = authorDao;
        this.bookDao = bookDao;
    }

    public int addBookWithNewAuthor(String authorName, String title, Integer year, String isbn) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false); //Не сохраняй каждый запрос сразу, жди команды
            try {
                int authorId = authorDao.add(conn, authorName);
                int bookId = bookDao.add(conn, title, authorId, year, isbn);
                conn.commit(); //Обе записи сохраняются разом
                return bookId;
            } catch (SQLException e) {
                conn.rollback(); //Любой из двух запросов упал - отменяем
                throw e; //Java не скомпилирует метод.
                // Он обещает вернуть int, а ветка catch ничего не возвращает: будет ошибка.
            }
        }
    }
}