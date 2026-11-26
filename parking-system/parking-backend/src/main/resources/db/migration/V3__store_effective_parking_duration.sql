DO $$
BEGIN
    -- On a fresh database Hibernate will create the table after Flyway runs.
    IF to_regclass('parking_orders') IS NOT NULL THEN
        ALTER TABLE parking_orders ADD COLUMN effective_duration_minutes bigint;

        -- Keep the original duration for sessions already issued. Pending orders
        -- retain their original price and grant the hours they will be charged for.
        UPDATE parking_orders
        SET effective_duration_minutes = CASE
            WHEN status = 'PAID' THEN duration_minutes
            ELSE billable_hours * 60::bigint
        END;

        ALTER TABLE parking_orders ALTER COLUMN effective_duration_minutes SET NOT NULL;
    END IF;
END $$;
