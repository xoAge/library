# Домашняя библиотека

Консольное приложение для учёта домашней библиотеки: книги и авторы хранятся в PostgreSQL, 
работа с базой - на чистом JDBC.

## Возможности

Меню программы:

```
1 - показать все книги
2 - найти по названию
3 - добавить книгу
4 - удалить книгу
5 - изменить книгу
6 - добавить книгу нового автора
7 - найти по автору
8 - авторы и число книг
0 - выход
```

- Поиск по части названия или имени автора без учёта регистра (`ILIKE`); 
символы `%` и `_` во вводе ищутся как обычные символы.
- Изменение книги: пустой ввод оставляет поле как было (`COALESCE`).
- Книга с новым автором добавляется в одной транзакции: либо сохраняются и автор, и книга, либо ничего.
- Статистика авторов показывает и авторов без книг (`LEFT JOIN` + `GROUP BY`).
- Ошибки базы переводятся в понятные сообщения: несуществующий автор, 
повтор ISBN или имени автора, неверный год.

## Технологии

- Java, Maven
- PostgreSQL, JDBC (`PreparedStatement`, транзакции)
- HikariCP - пул соединений
- Flyway - миграции схемы базы

## Структура

```
src/main/java/io/github/xoage/library/
├── App.java                 точка входа: пул, миграции, меню
├── db/Database.java         настройки из db.properties, пул HikariCP
├── dao/
│   ├── BookDao.java         SQL-запросы к таблице книг
│   └── AuthorDao.java       SQL-запросы к таблице авторов
├── service/
│   └── LibraryService.java  операции в транзакции
├── model/
│   ├── Book.java
│   ├── Author.java
│   └── AuthorStats.java     автор и количество его книг
└── ui/ConsoleMenu.java      консольное меню

src/main/resources/
├── db/migration/            миграции Flyway
│   ├── V1__create_tables.sql
│   ├── V2__initial_data.sql
│   └── V3__add_books_title_index.sql
└── db.properties.example    образец настроек подключения
```

## Схема базы

```
authors                       books
-------                       -----
id    PK                      id         PK
name  NOT NULL, UNIQUE        title      NOT NULL
                              author_id  FK -> authors.id
                              year       1450..2100
                              isbn       UNIQUE
```

## Запуск

**1. Создать пользователя и базу** (PostgreSQL):

```sql
CREATE USER library_app WITH PASSWORD 'ваш_пароль';
CREATE DATABASE library OWNER library_app;
```

**2. Настроить подключение.** 
Скопировать `src/main/resources/db.properties.example` в `db.properties` рядом с ним и вписать пароль:

```properties
db.url=jdbc:postgresql://localhost:5432/library
db.user=library_app
db.password=ваш_пароль
db.pool.size=5
```

`db.properties` не хранится в Git - в нём пароль.

**3. Запустить** класс `io.github.xoage.library.App` в IntelliJ IDEA.

При первом запуске Flyway сам создаст таблицы и добавит начальные данные (7 книг 4 авторов). 
Таблицы вручную создавать не нужно.

## Миграции

Каждое изменение схемы - отдельный файл `V<номер>__<описание>.sql` в `db/migration`. 
Flyway при запуске выполняет новые миграции и записывает их в таблицу `flyway_schema_history`. 
Уже применённые файлы не меняются: чтобы изменить схему, добавляется новая миграция.
