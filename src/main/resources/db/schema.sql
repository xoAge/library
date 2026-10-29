-- Схема базы "Домашняя библиотека".
-- IF NOT EXISTS

create table if not exists authors (
    id   integer generated always as identity primary key,
    name varchar(50) not null unique
);

create table if not exists books (
    id        integer generated always as identity primary key,
    title     varchar(100) not null check (length(trim(title)) > 0),
    author_id integer      not null references authors (id),
    year      integer      check (year between 1450 and 2100),
    isbn      varchar(17)  unique
);

create index if not exists idx_books_author_id on books (author_id);