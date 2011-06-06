-- Some initial data for the test database.

INSERT INTO author (last_name, first_name, middle_name, year_of_birth)
  VALUES ('Sagan', 'Carl', 'Edward', '1934');
INSERT INTO author (last_name, first_name, year_of_birth)
  VALUES ('Odersky', 'Martin', '1958');
INSERT INTO author (last_name, first_name) VALUES ('Spoon', 'Lex');
INSERT INTO author (last_name, first_name) VALUES ('Venners', 'Bill');

INSERT INTO book (title, author_id)
  SELECT 'The Demon-Haunted World: Science as a Candle in the Dark',
    id FROM author WHERE last_name = 'Sagan';
INSERT INTO book (title, author_id) SELECT 'Cosmos', id FROM author
  WHERE last_name = 'Sagan';

INSERT INTO book (title, author_id)
  SELECT 'Programming in Scala', ID FROM author
    WHERE last_name = 'Odersky';
UPDATE book SET co_author_id = (SELECT id FROM author WHERE last_name = 'Spoon')
  WHERE title = 'Programming in Scala';
