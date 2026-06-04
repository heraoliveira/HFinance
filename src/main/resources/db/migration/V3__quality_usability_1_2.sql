ALTER TABLE categories ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1;
ALTER TABLE categories ADD COLUMN updated_at TEXT;

UPDATE categories
   SET updated_at = created_at
 WHERE updated_at IS NULL;

ALTER TABLE transactions ADD COLUMN recurrence_group_id TEXT;
ALTER TABLE transactions ADD COLUMN recurrence_type TEXT;
ALTER TABLE transactions ADD COLUMN recurrence_index INTEGER;
ALTER TABLE transactions ADD COLUMN recurrence_total INTEGER;

DROP INDEX IF EXISTS idx_categories_name_type;

CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_active_name_type
    ON categories (LOWER(TRIM(name)), type)
    WHERE is_active = 1;

CREATE INDEX IF NOT EXISTS idx_categories_type_active
    ON categories (type, is_active);

CREATE INDEX IF NOT EXISTS idx_transactions_payment_method
    ON transactions (payment_method);

CREATE INDEX IF NOT EXISTS idx_transactions_recurrence_group
    ON transactions (recurrence_group_id);
