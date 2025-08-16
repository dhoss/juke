create table users(
  id integer not null generated always as identity primary key,
  user_name varchar(50) not null unique,
  password varchar(82) not null unique,
  email varchar(100) not null unique,
  is_verified boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);

create table roles(
  id integer not null generated always as identity primary key,
  name varchar(20) not null unique,
  created_on timestamptz not null default now()
);

create table user_roles(
  id integer not null generated always as identity primary key,
  user_id integer not null references users(id),
  role_id integer not null references roles(id)
);

-- create default user
insert into users(user_name, email, password)
values ('admin', 'fart@mailinator.com', '$2a$12$MphK9zyCUxwY2/cAFfoNB.G2364H3ZzwTS6llVD2IdLENbtgmjEJ2');

insert into roles(name) values('admin'), ('regular_user');

insert into user_roles(user_id, role_id)
values ((select id from users where user_name='admin'), (select id from roles where name='admin')),
       ((select id from users where user_name='admin'), (select id from roles where name='regular_user'));
