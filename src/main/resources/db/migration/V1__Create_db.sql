--liquidbase farmatted sql

--changeset blezek:1
create table users (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email varchar(512) NOT NULL,
  uid varchar(255) NOT NULL,
  password varchar(512),
  salt varchar(64),
  activated boolean,
  activation_hash varchar(256),
  CONSTRAINT users_email UNIQUE ( email )
  );

  create index users_index_hash on users ( activation_hash );
  create index users_index_email on users ( email );
  create index users_index_uid on users ( uid );
  
  
create table watches (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  name varchar(255),
  description varchar(255),
  uid varchar(512),
  cron varchar(128) NOT NULL,
  timezone varchar(128) NOT NULL,
  explanation varchar(128) NOT NULL,
  worry int DEFAULT 10,
  status varchar(32),
  active boolean,
  consecutive_failed_checks int DEFAULT 0,
  last_check TIMESTAMP,
  next_check TIMESTAMP,
  expected TIMESTAMP,
  CONSTRAINT watches_uid UNIQUE ( uid ),
  CONSTRAINT watches_fk_1 FOREIGN KEY ( user_id ) REFERENCES users ( id ) ON DELETE CASCADE
  );

create index watches_index_user_id on watches ( user_id );
create index watches_index_uid on watches ( uid );

create table heartbeats (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  watch_id BIGINT NOT NULL,
  instant TIMESTAMP,
  message varchar(150),
  status varchar(16),
  CONSTRAINT heartbeats_fk_1 FOREIGN KEY ( watch_id ) REFERENCES watches ( id ) ON DELETE CASCADE
  );

create index heartbeats_index_instant on heartbeats ( watch_id, instant );

create table watch_user (
  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  watch_id BIGINT,
  user_id BIGINT
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
