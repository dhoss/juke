create type news_type as enum ('news', 'motd');

create table news(
  id integer not null generated always as identity primary key,
  title varchar(100) not null unique,
  author_id integer not null references authors(id),
  body varchar not null,
  type news_type not null,
  created_on timestamptz not null default now(),
  updated_on timestamptz,
  published_on timestamptz
);