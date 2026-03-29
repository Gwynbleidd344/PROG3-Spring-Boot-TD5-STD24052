CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_db_manager WITH PASSWORD '123456';

GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;
GRANT CREATE ON DATABASE mini_dish_db TO mini_dish_db_manager;

GRANT ALL PRIVILEGES ON SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO mini_dish_db_manager;