/* CREATE DATABASE examples_db;
   CREATE USER examples_app WITH PASSWORD 'password'; */


DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;


CREATE TABLE categories (
    id         serial primary key not null,
    name   text not null
);

CREATE TABLE products (
    id              serial primary key not null,
    category_id     int not null references categories(id),
    name   text not null
);




GRANT SELECT,INSERT,UPDATE,DELETE  ON  categories          TO examples_app;
GRANT SELECT,USAGE                 ON  categories_id_seq   TO examples_app;
GRANT SELECT,INSERT,UPDATE,DELETE  ON  products            TO examples_app;
GRANT SELECT,USAGE                 ON  products_id_seq     TO examples_app;
