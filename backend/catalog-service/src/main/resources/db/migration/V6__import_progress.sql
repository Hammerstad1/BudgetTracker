CREATE TABLE IF NOT EXISTS import_progress (
    id BIGSERIAL PRIMARY KEY,
    country_code TEXT NOT NULL UNIQUE,
    last_page INT NOT NULL,
    total_imported INT NOT NULL DEFAULT 0,
    total_updated INT NOT NULL DEFAULT 0,
    last_run_status TEXT,
    last_run_at TIMESTAMPTZ
);