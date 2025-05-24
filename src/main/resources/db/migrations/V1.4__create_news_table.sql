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

-- create a motd
insert into news(title, author_id, body, type, published_on)
values(
  'Welcome to Juke',
  (select id from authors where email = 'devin.austin@gmail.com'),
  'Welcome to the Juke CMS',
  'motd',
  now()
);

-- create some news
insert into news(title, author_id, body, type, published_on)
values(
  'Juke News',
  (select id from authors where email = 'devin.austin@gmail.com'),
  'News about juke',
  'news',
  now()
);

insert into news(title, author_id, body, type, published_on)
values(
  'More Juke News',
  (select id from authors where email = 'devin.austin@gmail.com'),
  'More news about juke',
  'news',
  now()
);
