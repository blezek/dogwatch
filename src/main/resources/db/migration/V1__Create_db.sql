--liquidbase farmatted sql

--changeset blezek:1

create table watches (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name varchar(255),
  uid varchar(512),
  frequency int,
  worry int,
  active boolean,
  CONSTRAINT watches_uid UNIQUE ( uid )
  );

create table heartbeats (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  watch_id BIGINT NOT NULL,
  instant TIMESTAMP
  );

create table watch_user (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  watch_id BIGINT,
  user_id BIGINT
  );

create table users (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email varchar(512) NOT NULL,
  password varchar(512),
  activated boolean,
  activation_hash varchar(256),
  CONSTRAINT users_email UNIQUE ( email )
  );