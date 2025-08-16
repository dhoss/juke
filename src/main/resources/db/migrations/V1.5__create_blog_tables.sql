create table blogs(
  id integer not null generated always as identity primary key,
  slug varchar(30) not null unique,
  name varchar(200) not null,
  owner integer not null references authors(id),
  created_on timestamptz not null,
  updated_on timestamptz
);

create table blog_posts(
  id integer not null generated always as identity primary key,
  author integer not null references authors(id),
  blog integer not null references blogs(id),
  title varchar(200) not null unique,
  slug varchar(30) not null unique,
  body varchar not null,
  parent integer references blog_posts(id),
  approved boolean default false,
  created_on timestamptz not null,
  published_on timestamptz,
  updated_on timestamptz
);