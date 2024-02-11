-- Init database for postgres

CREATE TABLE IF NOT EXISTS files
(
    id serial not null primary key,
    name character varying(255) NOT NULL,
    location character varying(255) NOT NULL,
    bytes bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone NOT NULL
);