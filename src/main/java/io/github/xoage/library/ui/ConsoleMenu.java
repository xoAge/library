package io.github.xoage.library.ui;

import io.github.xoage.library.dao.AuthorDao;
import io.github.xoage.library.dao.BookDao;
import io.github.xoage.library.model.Author;
import io.github.xoage.library.model.Book;
import io.github.xoage.library.service.LibraryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final BookDao bookDao;
    private final AuthorDao authorDao;
    private final LibraryService service;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(BookDao bookDao, AuthorDao authorDao, LibraryService service) {
        this.bookDao = bookDao;
        this.authorDao = authorDao;
        this.service = service;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("1 - показать все книги");
            System.out.println("2 - найти по названию");
            System.out.println("3 - добавить книгу");
            System.out.println("4 - удалить книгу");
            System.out.println("5 - изменить книгу");
            System.out.println("6 - добавить книгу нового автора");
            System.out.println("7 - найти по автору");
            System.out.println("0 - выход");
            System.out.print("Выбор: ");
            String choice = scanner.nextLine().trim();

            try {                                       //try внутри while, потому что работает от try к catch, если будет снаружи: ошибка ловится снаружи, а catch уже нет.
                switch (choice) {
                    case "1" -> printBooks(bookDao.findAll());
                    case "2" -> search();
                    case "3" -> addBook();
                    case "4" -> deleteBook();
                    case "5" -> updateBook();
                    case "6" -> addBookWithNewAuthor();
                    case "7" -> searchByAuthor();
                    case "0" -> {
                        System.out.println("Пока!");
                        return;
                    }
                    default -> System.out.println("Нет такого пункта");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести целое число");
            } catch (SQLException e) {
                System.out.println("Ошибка: " + explain(e));
            }
        }
    }

    private static String explain(SQLException e) {
        String state = e.getSQLState();
        if (state == null) {
            return e.getMessage();
        }
        return switch (state) {
            case "23503" -> "нет автора с таким номером";
            case "23505" -> "такая запись уже есть: автор с таким именем или книга с таким ISBN";
            case "23514" -> "название не может быть пустым, год - от 1450 до 2100";
            default -> "база данных ответила: " + e.getMessage();
        };
    }

    private void search() throws SQLException {
        System.out.print("Часть названия: ");
        String part = scanner.nextLine().trim(); //nextInt() читает число, а Enter после него оставляет во вводе.
                                                // Следующий nextLine() подхватывает этот пустой Enter, поэтому везде nextLine()
        printBooks(bookDao.findByTitle(part));
    }

    private void searchByAuthor() throws SQLException {
        System.out.print("Часть имени автора: ");
        String part = scanner.nextLine().trim();
        printBooks(bookDao.findByAuthor(part));
    }

    private void addBook() throws SQLException {
        System.out.print("Название: ");
        String title = scanner.nextLine().trim();

        System.out.println("Авторы:");
        for (Author author : authorDao.findAll()) {
            System.out.println("  " + author.id() + ". " + author.name());
        }
        System.out.print("Номер автора: ");
        int authorId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Год (Enter - неизвестен): ");
        String yearText = scanner.nextLine().trim();
        Integer year = yearText.isEmpty() ? null : Integer.parseInt(yearText);

        System.out.print("ISBN (Enter - нет): ");
        String isbnText = scanner.nextLine().trim();
        String isbn = isbnText.isEmpty() ? null : isbnText;

        int id = bookDao.add(title, authorId, year, isbn);
        System.out.println("Книга добавлена, id = " + id);
    }

    private void deleteBook() throws SQLException {
        System.out.print("id книги для удаления: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        if (bookDao.deleteById(id)) {
            System.out.println("Книга удалена");
        } else {
            System.out.println("Книги с id " + id + " нет");
        }
    }

    private void updateBook() throws SQLException {
        System.out.print("id книги: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Новое название (Enter - оставить как есть): ");
        String titleText = scanner.nextLine().trim();
        String newTitle = titleText.isEmpty() ? null : titleText;

        System.out.print("Новый год (Enter - оставить как есть): ");
        String yearText = scanner.nextLine().trim();
        Integer newYear = yearText.isEmpty() ? null : Integer.parseInt(yearText);

        if (newTitle == null && newYear == null) {
            System.out.println("Ничего не изменено");
            return;
        }

        if (bookDao.update(id, newTitle, newYear)) {
            System.out.println("Книга изменена");
        } else {
            System.out.println("Книги с id " + id + " нет");
        }
    }

    private void addBookWithNewAuthor() throws SQLException {
        System.out.print("Имя нового автора: ");
        String authorName = scanner.nextLine().trim();

        System.out.print("Название книги: ");
        String title = scanner.nextLine().trim();

        System.out.print("Год (Enter - неизвестен): ");
        String yearText = scanner.nextLine().trim();
        Integer year = yearText.isEmpty() ? null : Integer.parseInt(yearText);

        System.out.print("ISBN (Enter - нет): ");
        String isbnText = scanner.nextLine().trim();
        String isbn = isbnText.isEmpty() ? null : isbnText;

        int bookId = service.addBookWithNewAuthor(authorName, title, year, isbn);
        System.out.println("Автор и книга добавлены, id книги = " + bookId);
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("Ничего не найдено");
            return;
        }
        for (Book book : books) {
            System.out.printf("  %3d  %-26s %-18s %s%n",
                    book.id(), book.title(), book.author().name(),
                    book.year() == null ? "-" : book.year());
        }
    }
}
