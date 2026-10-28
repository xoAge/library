package io.github.xoage.library;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {

    public static void main(String[] args) {
        Database db = Database.fromConfig();
        System.out.println("Подключаюсь к " + db.url() + " ...");

        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT current_database(), current_user, version()")) {

            rs.next();
            System.out.println("Готово!");
            System.out.println("  база:         " + rs.getString(1));
            System.out.println("  пользователь: " + rs.getString(2));
            System.out.println("  сервер:       " + rs.getString(3));
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
        }
    }
}
