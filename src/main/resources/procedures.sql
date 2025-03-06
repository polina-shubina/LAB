--CREATE USER admin WITH PASSWORD 'admin';
--GRANT ALL PRIVILEGES ON DATABASE online_store TO admin;

--CREATE USER guest WITH PASSWORD 'guest';
--GRANT CONNECT ON DATABASE online_store TO guest;
--GRANT SELECT ON ALL TABLES IN SCHEMA public TO guest;
CREATE EXTENSION IF NOT EXISTS dblink;
CREATE OR REPLACE PROCEDURE create_database(db_name TEXT)
AS $$
BEGIN
    PERFORM dblink_exec(
            'host=localhost dbname=postgres user=' || current_user,
            'CREATE DATABASE ' || quote_ident(db_name)
        );
    RAISE NOTICE 'Database "%" created.', db_name;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE drop_database(db_name TEXT)
AS $$
BEGIN
    PERFORM dblink_exec(
            'host=localhost dbname=postgres user=' || current_user,
            'DO $inner$
             BEGIN
               PERFORM pg_terminate_backend(pid)
               FROM pg_stat_activity
               WHERE datname = ' || quote_literal(db_name) || ' AND pid <> pg_backend_pid();
             END $inner$;'
        );
    PERFORM dblink_exec(
            'host=localhost dbname=postgres user=' || current_user,
            'DROP DATABASE IF EXISTS ' || quote_ident(db_name)
        );
    RAISE NOTICE 'Database "%" dropped.', db_name;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE create_table(db_name TEXT)
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND lower(table_name) = lower(db_name)
    ) THEN
         RAISE NOTICE 'Table "%" already exists.', db_name;
    ELSE
         EXECUTE format('CREATE TABLE %I (
            customer_id SERIAL PRIMARY KEY,
            full_name TEXT NOT NULL,
            good_id INT NOT NULL,
            good_name TEXT NOT NULL,
            price DECIMAL(10, 2) NOT NULL
         )', db_name);
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE clear_table(db_name TEXT)
AS $$
BEGIN
    EXECUTE format('TRUNCATE TABLE %I', db_name);
    RAISE NOTICE 'Table "%" cleared.', db_name;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE add_data(db_name TEXT, customer_id INT, full_name TEXT, good_id INT, good_name TEXT, price DECIMAL)
AS $$
BEGIN
    EXECUTE format('INSERT INTO %I (customer_id, full_name, good_id, good_name, price) VALUES ($1, $2, $3, $4, $5)', db_name)
    USING customer_id, full_name, good_id, good_name, price;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION search_by_good_name(db_name TEXT, search_term TEXT)
RETURNS TABLE(customer_id INT, full_name TEXT, good_id INT, good_name TEXT, price DECIMAL)
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
          AND lower(table_name) = lower(db_name)
    ) THEN
        RETURN;
    ELSE
        RETURN QUERY EXECUTE format('SELECT * FROM %I WHERE good_name ILIKE $1', db_name)
            USING '%' || search_term || '%';
    END IF;
END;
$$ LANGUAGE plpgsql;