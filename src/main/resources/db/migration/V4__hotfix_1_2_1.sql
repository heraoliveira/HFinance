UPDATE categories
   SET is_active = 0,
       updated_at = strftime('%Y-%m-%dT%H:%M:%f','now')
 WHERE type = 'INCOME'
   AND is_default = 1
   AND name IN ('Freelance', 'Presente')
   AND (
        EXISTS (SELECT 1 FROM transactions WHERE transactions.category_id = categories.id)
        OR EXISTS (SELECT 1 FROM budgets WHERE budgets.category_id = categories.id)
   );

DELETE FROM categories
 WHERE type = 'INCOME'
   AND is_default = 1
   AND name IN ('Freelance', 'Presente')
   AND NOT EXISTS (SELECT 1 FROM transactions WHERE transactions.category_id = categories.id)
   AND NOT EXISTS (SELECT 1 FROM budgets WHERE budgets.category_id = categories.id);

CREATE INDEX IF NOT EXISTS idx_transactions_recurrence_group_date
    ON transactions (recurrence_group_id, transaction_date);
