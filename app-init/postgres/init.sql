-- Init database for postgres

CREATE TABLE IF NOT EXISTS files
(
    id serial not null primary key,
    name character varying(255) NOT NULL,
    location character varying(255) NOT NULL,
    bytes bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);

DROP TYPE IF EXISTS storage_type CASCADE;
CREATE TYPE storage_type AS ENUM ('STAGING', 'PERMANENT');
CREATE CAST (varchar AS storage_type) WITH INOUT AS IMPLICIT;

CREATE TABLE IF NOT EXISTS storages
(
    id serial not null primary key,
    st_type storage_type NOT NULL,
    bucket character varying(255) NOT NULL,
    path character varying(255) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);