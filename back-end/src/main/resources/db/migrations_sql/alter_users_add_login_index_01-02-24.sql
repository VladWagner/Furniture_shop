-- Добавить B-tree index полю login для ускоренного поиска
CREATE INDEX login_index ON users (login);