-- TODO: add versions
create table pages (
  id integer not null generated always as identity primary key,
  author_id integer not null references authors(id),
  title varchar(255) unique not null,
  is_deleted boolean not null default false,
  slug varchar(30) not null,
  layout_id integer not null references layouts(id),
  created_on timestamptz not null default now(),
  updated_on timestamptz,
  published_on timestamptz
);