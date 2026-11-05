package io.github.xoage.library;

import io.github.xoage.library.dao.AuthorDao;
import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.db.Database;
import io.github.xoage.library.service.LibraryService;
import io.github.xoage.library.ui.ConsoleMenu;
import org.flywaydb.core.Flyway;

public class App {

    public static void main(String[] args) {
        try (Database db = Database.fromConfig()) {
            Flyway.configure()
                    .dataSource(db.dataSource())
                    .load()                     //собрать готовый объект
                    .migrate();

            BookDao bookDao = new BookDao(db);
            AuthorDao authorDao = new AuthorDao(db);
            LibraryService service = new LibraryService(db, authorDao, bookDao);

            ConsoleMenu menu = new ConsoleMenu(bookDao, authorDao, service);
            menu.run();
        } catch (RuntimeException e) {
            System.out.println("Не удалось запустить программу: " + e.getMessage());
            System.out.println("Проверь, что PostgreSQL запущен и настройки в db.properties верные.");
        }
    }
}
