ALTER TABLE build
    ADD COLUMN duration_ms    BIGINT,
    ADD COLUMN failure_reason VARCHAR(255);
