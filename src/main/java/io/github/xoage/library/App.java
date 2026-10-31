package io.github.xoage.library;

import io.github.xoage.library.dao.AuthorDao;
import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.db.Database;
import io.github.xoage.library.db.DatabaseInitializer;
import io.github.xoage.library.ui.ConsoleMenu;

import java.sql.SQLException;


public class App {

    public static void main(String[] args) {
        Database db = Database.fromConfig();
        try {
            new DatabaseInitializer(db).init();

            ConsoleMenu menu = new ConsoleMenu(new BookDao(db), new AuthorDao(db));
            menu.run();
        } catch (SQLException e) {           //для инициализации бд new DatabaseInitializer(db).init();
            System.out.println("Ошибка базы данных: " + e.getMessage());
        }
    }
}
