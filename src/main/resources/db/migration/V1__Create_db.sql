--liquidbase farmatted sql

--changeset blezek:1

create table watches (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  name varchar(255),
  description varchar(255),
  uid varchar(512),
  cron varchar(128) NOT NULL,
  worry int,
  status varchar(32),
  active boolean,
  last_check TIMESTAMP,
  next_check TIMESTAMP,
  CONSTRAINT watches_uid UNIQUE ( uid )
  );

create table heartbeats (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  watch_id BIGINT NOT NULL,
  instant TIMESTAMP,
  message varchar(150),
  status varchar(16)
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
  salt varchar(64),
  activated boolean,
  activation_hash varchar(256),
  CONSTRAINT users_email UNIQUE ( email )
  );
  
create table user_role (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role varchar(32) NOT NULL
);
  
create table permissions (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  role varchar(32) NOT NULL,
  permission varchar(256)
);
  
insert into permissions ( role, permission ) values ( 'user', 'user' );
insert into permissions ( role, permission ) values ( 'admin', 'admin' );
