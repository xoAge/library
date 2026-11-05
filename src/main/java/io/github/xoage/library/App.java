package io.github.xoage.library;

import io.github.xoage.library.dao.AuthorDao;
import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.db.Database;
import io.github.xoage.library.db.DatabaseInitializer;
import io.github.xoage.library.service.LibraryService;
import io.github.xoage.library.ui.ConsoleMenu;

import java.sql.SQLException;


public class App {

    public static void main(String[] args) {
        try (Database db = Database.fromConfig()) {
            new DatabaseInitializer(db).init();

            BookDao bookDao = new BookDao(db);
            AuthorDao authorDao = new AuthorDao(db);
            LibraryService service = new LibraryService(db, authorDao, bookDao);

            ConsoleMenu menu = new ConsoleMenu(bookDao, authorDao, service);
            menu.run();
        } catch (SQLException e) {           //для инициализации бд new DatabaseInitializer(db).init();
            System.out.println("Ошибка базы данных: " + e.getMessage());
        }
    }
}
