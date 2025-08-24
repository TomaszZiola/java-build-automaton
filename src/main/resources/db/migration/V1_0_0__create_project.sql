CREATE SEQUENCE public.project_sq INCREMENT 1 START WITH 1 MINVALUE 1;

CREATE TABLE project
(
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(255),
    repository_name VARCHAR(255),
    local_path      VARCHAR(255),
    build_tool      VARCHAR(255)
);
