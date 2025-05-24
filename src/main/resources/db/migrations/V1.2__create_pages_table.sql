-- TODO: add versions
create table pages (
  id integer not null generated always as identity primary key,
  author_id integer not null references authors(id),
  title varchar(255) unique not null,
  is_deleted boolean not null default false,
  slug varchar(30) not null,
  layout_id integer not null references layouts(id),
  body varchar, -- TODO: index with tsearch2
  created_on timestamptz not null default now(),
  updated_on timestamptz,
  published_on timestamptz
);

insert into pages(author_id, title, slug, layout_id, published_on)
values(
(select id from authors where email = 'devin.austin@gmail.com'),
'Juke CMS - Welcome', 'front-page',
(select id from layouts where slug = 'default'),
now());

insert into pages(author_id, title, slug, layout_id, body, published_on)
values(
(select id from authors where email = 'devin.austin@gmail.com'),
'Juke CMS - About',
'about',
(select id from layouts where slug = 'default'),
E'## About Juke Text  \n'
'Juke is a spiritual clone of phpnuke.',
now());
