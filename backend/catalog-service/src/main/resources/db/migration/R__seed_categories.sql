INSERT INTO category (name) VALUES
    ('Grocery'),
    ('Beverages'),
    ('Snacks'),
    ('Dairy'),
    ('Household')
ON CONFLICT (name) DO NOTHING;

WITH
    grocery AS (SELECT id FROM category WHERE name = 'Grocery'),
    beverages AS (SELECT id FROM category WHERE name = 'Beverages'),
    snacks AS (SELECT id FROM category WHERE name = 'Snacks'),
    dairy AS (SELECT id FROM category WHERE name = 'Dairy')
SELECT 1;