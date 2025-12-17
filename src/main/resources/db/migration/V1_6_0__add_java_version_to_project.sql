ALTER TABLE project
    ADD COLUMN java_version_major INTEGER;

UPDATE project SET java_version_major = 21 WHERE java_version_major IS NULL;

ALTER TABLE project
    ALTER COLUMN java_version_major SET NOT NULL;
