CREATE TABLE todo_list
(
  id SERIAL PRIMARY KEY,
  title TEXT
);

CREATE TABLE todos
(
  id SERIAL PRIMARY KEY,
  description TEXT,
  done BOOLEAN,
  list INTEGER REFERENCES todo_list
);

INSERT INTO todo_list (title) VALUES
  ('First Todo List');

INSERT INTO todos (description, done, list) VALUES
  ('Add a second todo', TRUE, 1),
  ('Add a third todo', FALSE, 1);
