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

-- :name get-lists-joined-todos :? :*
-- :doc join todos w/ list name
SELECT todos.id, description, done, list, title
FROM todos, todo_list;

-- :name get-todos :? :*
-- :doc returns the list of todos for a given todo_list :id
SELECT * FROM todos
WHERE list = :id

-- :name create-todo! :! :1
-- :doc puts a new todo in the database
INSERT INTO todos
(id, description, done, list)
VALUES
(DEFAULT, :description, FALSE, 1)

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
