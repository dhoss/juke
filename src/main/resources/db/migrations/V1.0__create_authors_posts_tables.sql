create table authors (
  id integer not null generated always as identity primary key,
  user_name text not null unique,
  email text not null unique,
  created_on timestamptz,
  updated_on timestamptz
);

-- create initial user
insert into authors(user_name, email) values('Devin', 'devin.austin@gmail.com');

-- TODO: create posts table
