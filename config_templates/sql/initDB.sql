-- table cities
DROP TABLE IF EXISTS cities CASCADE;
DROP SEQUENCE IF EXISTS city_seq;

CREATE SEQUENCE city_seq START 100000;

CREATE TABLE cities (
  id INTEGER PRIMARY KEY DEFAULT nextval('city_seq'),
  name TEXT NOT NULL,
  alias TEXT NOT NULL,
  CONSTRAINT name_idx UNIQUE (alias)
);

-- table users
DROP TABLE IF EXISTS users CASCADE;
DROP SEQUENCE IF EXISTS user_seq;
DROP TYPE IF EXISTS user_flag;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');

CREATE SEQUENCE user_seq START 100000;

CREATE TABLE users (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT NOT NULL,
  email     TEXT NOT NULL,
  flag      user_flag NOT NULL,
  city_id   INTEGER DEFAULT NULL REFERENCES cities(id)
);

CREATE UNIQUE INDEX email_idx ON users (email);

-- table projects
DROP TABLE IF EXISTS projects CASCADE;
DROP SEQUENCE IF EXISTS project_seq;

CREATE SEQUENCE project_seq START 100000;

CREATE TABLE projects (
  id INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
  name TEXT NOT NULL,
  description TEXT DEFAULT NULL
);

-- table groups
DROP TABLE IF EXISTS groups CASCADE;
DROP SEQUENCE IF EXISTS group_seq;
DROP TYPE IF EXISTS group_type;

CREATE TYPE group_type AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');

CREATE SEQUENCE group_seq START 100000;

CREATE TABLE groups (
  id INTEGER PRIMARY KEY DEFAULT nextval('group_seq'),
  name TEXT NOT NULL,
  type group_type NOT NULL,
  project_id INTEGER REFERENCES projects(id)
);

-- table for connection user has many groups
DROP TABLE IF EXISTS users_groups CASCADE;

CREATE TABLE users_groups (
  user_id INTEGER REFERENCES users(id),
  group_id INTEGER REFERENCES groups(id),
  PRIMARY KEY (user_id, group_id)
);
