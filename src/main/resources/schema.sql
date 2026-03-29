CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE ingredient_category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');
CREATE TYPE mouvement_type AS ENUM ('IN', 'OUT');

CREATE TABLE dish (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    dish_type     dish_type    NOT NULL,
    selling_price NUMERIC
);

CREATE TABLE ingredient (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255)          NOT NULL,
    price    NUMERIC               NOT NULL,
    category ingredient_category   NOT NULL
);

CREATE TABLE dish_ingredient (
    id                SERIAL PRIMARY KEY,
    id_dish           INT     NOT NULL REFERENCES dish(id),
    id_ingredient     INT     NOT NULL REFERENCES ingredient(id),
    quantity_required NUMERIC NOT NULL,
    unit              unit_type NOT NULL
);

CREATE TABLE stock_movement (
    id                SERIAL PRIMARY KEY,
    id_ingredient     INT             NOT NULL REFERENCES ingredient(id),
    quantity          NUMERIC         NOT NULL,
    type              mouvement_type  NOT NULL,
    unit              unit_type       NOT NULL,
    creation_datetime TIMESTAMP       NOT NULL DEFAULT NOW()
);