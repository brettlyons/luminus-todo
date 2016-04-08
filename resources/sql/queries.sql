-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

-- :name get-lists :? :*
-- :doc returns the title and id of a todo_list
SELECT id, title
FROM todo_list;

-- :name get-todos :? :*
-- :doc returns the todos for a given todo_list's id
SELECT * FROM todos
WHERE list = :list_id

-- :name create-todo! :! :1
-- :doc puts a new todo in the database
INSERT INTO todos
(id, description, done, list)
VALUES
(DEFAULT, :description, FALSE, :list_id)

-- :name create-list! :! :1
-- :doc makes a new empty todo-list int he db
INSERT INTO todo_list
(id, title)
VALUES
(DEFAULT, :title)

-- :name update-done! :! :1
-- :doc changes the state of the "done" column for 1 todo.
UPDATE todos
SET done = :done
WHERE id = :id

-- :name delete-todo! :! :1
-- :doc Deletes the todo with the given id
DELETE FROM todos
WHERE id = :id
