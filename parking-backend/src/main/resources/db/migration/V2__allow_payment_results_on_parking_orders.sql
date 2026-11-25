-- Existing installations created this table and its enum check through Hibernate.
-- On a fresh database Hibernate creates the table with all three statuses.
ALTER TABLE IF EXISTS parking_orders DROP CONSTRAINT IF EXISTS parking_orders_status_check;
ALTER TABLE IF EXISTS parking_orders ADD CONSTRAINT parking_orders_status_check
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'PAYMENT_FAILED'));
