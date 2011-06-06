-- NOTE: SQLite doesn't have an autoincrement keyword. However, per
-- http://www.sqlite.org/autoinc.html
--
-- In SQLite, every row of every table has an 64-bit signed integer ROWID.
-- The ROWID for each row is unique among all rows in the same table. You
-- can access the ROWID of an SQLite table using one the special column
-- names ROWID, _ROWID_, or OID.
--
-- If a table contains a column of type INTEGER PRIMARY KEY, then that
-- column becomes an alias for the ROWID. You can then access the ROWID
-- using any of four different names, the original three names described
-- above or the name given to the INTEGER PRIMARY KEY column. All these
-- names are aliases for one another and work equally well in any context.
--
-- When a new row is inserted into an SQLite table, the ROWID can either be
-- specified as part of the INSERT statement or it can be assigned
-- automatically by the database engine.

DROP TABLE IF EXISTS author;
CREATE TABLE author (
  id             INTEGER PRIMARY KEY,
  last_name      VARCHAR(50) NOT NULL,
  first_name     VARCHAR(50) NOT NULL,
  middle_name    VARCHAR(50) NULL,
  nationality    VARCHAR(100) DEFAULT 'US',
  year_of_birth  VARCHAR(4)
);

DROP TABLE IF EXISTS book;
CREATE TABLE book (
  id           INTEGER PRIMARY KEY,
  title        VARCHAR(100) NOT NULL,
  author_id    INTEGER NOT NULL,
  co_author_id INTEGER,

  FOREIGN KEY (author_id) REFERENCES author(id),
  FOREIGN KEY (co_author_id) REFERENCES author(id)
);

DROP TABLE IF EXISTS borrower;
CREATE TABLE borrower (
  id        INTEGER PRIMARY KEY,
  phone_num VARCHAR(20) NOT NULL,
  address   TEXT NOT NULL
);

DROP TABLE IF EXISTS borrowal;
CREATE TABLE borrowal (
  id                       INTEGER PRIMARY KEY,
  book_id                  INTEGER NOT NULL,
  borrower_id              INTEGER NOT NULL,
  scheduled_to_return_on   DATE NOT NULL,
  returned_on              TIMESTAMP,
  num_nonreturn_phonecalls INT,

  FOREIGN KEY (book_id) REFERENCES book(id),
  FOREIGN KEY (borrower_id) REFERENCES borrower(id)
);
