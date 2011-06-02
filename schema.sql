CREATE TABLE author (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nationality    VARCHAR(100),
  year_of_birth  VARCHAR(4),
)

CREATE TABLE book (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(100) NOT NULL,
  author_id    BIGINT NOT NULL,
  co_author_id BIGINT,

  FOREIGN KEY (author_id) REFERENCES author(id),
  FOREIGN KEY (co_author_id) REFERENCES author(id)
)

CREATE TABLE borrower (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone_num VARCHAR(20) NOT NULL,
  address   TEXT NOT NULL
)

CREATE TABLE borrowal (
  id BIGINT                AUTO_INCREMENT PRIMARY KEY,
  book_id                  BIGINT NOT NULL,
  borrower_id              BIGINT NOT NULL,
  scheduled_to_return_on   DATE NOT NULL,
  returned_on              TIMESTAMP,
  num_nonreturn_phonecalls INT,

  FOREIGN KEY (book_id) REFERENCES book(id),
  FOREIGN KEY (borrower_id) REFERENCES borrower(id)
)
