\set ON_ERROR_STOP on

SELECT 'CREATE DATABASE catalog_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'catalog_db')\gexec

SELECT 'CREATE DATABASE basket_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'basket_db')\gexec

SELECT 'CREATE DATABASE pricing_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'pricing_db')\gexec

SELECT 'CREATE DATABASE budget_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'budget_db')\gexec
