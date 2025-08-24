CREATE SEQUENCE public.build_sq INCREMENT 1 START WITH 1 MINVALUE 1;

CREATE TABLE build
(
    id         BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status     VARCHAR(255),
    start_time TIMESTAMP,
    end_time   TIMESTAMP,
    logs       TEXT,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES project (id)
);
