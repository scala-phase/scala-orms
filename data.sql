-- Some initial data for the test database.

INSERT INTO author (last_name, first_name, middle_name, year_of_birth)
  VALUES ('Sagan', 'Carl', 'Edward', '1934');
INSERT INTO author (last_name, first_name, year_of_birth)
  VALUES ('Odersky', 'Martin', '1958');
INSERT INTO author (last_name, first_name) VALUES ('Spoon', 'Lex');
INSERT INTO author (last_name, first_name) VALUES ('Venners', 'Bill');

INSERT INTO book (title)
  VALUES ('The Demon-Haunted World: Science as a Candle in the Dark');
INSERT INTO book (title) VALUES ('Cosmos');
INSERT INTO book (title) VALUES ('Programming in Scala');

INSERT INTO bookauthor (book_id, author_id)
  SELECT book.id, author.id FROM book, author
   WHERE book.title = 'The Demon-Haunted World: Science as a Candle in the Dark'
     AND author.last_name = 'Sagan';

INSERT INTO bookauthor (book_id, author_id)
  SELECT book.id, author.id FROM book, author
   WHERE book.title = 'Cosmos' AND author.last_name = 'Sagan';

INSERT INTO bookauthor (book_id, author_id)
  SELECT book.id, author.id FROM book, author
   WHERE book.title = 'Programming in Scala' AND author.last_name = 'Odersky';

INSERT INTO bookauthor (book_id, author_id)
  SELECT book.id, author.id FROM book, author
   WHERE book.title = 'Programming in Scala' AND author.last_name = 'Spoon';

INSERT INTO bookauthor (book_id, author_id)
  SELECT book.id, author.id FROM book, author
   WHERE book.title = 'Programming in Scala' AND author.last_name = 'Venners';

