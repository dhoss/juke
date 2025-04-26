create table pages (
  id integer not null generated always as identity primary key,
  author_id integer not null references authors(id),
  title varchar(255) unique not null,
  is_deleted boolean not null default false,
  slug varchar(30) not null,
  created_on timestamptz not null default now(),
  updated_on timestamptz,
  published_on timestamptz
);

create type page_component_type as enum('news', 'post', 'motd', 'sidebar', 'header', 'footer', 'top_nav_bar');

create table page_components(
  id integer not null generated always as identity primary key,
  author_id integer not null references authors(id),
  title varchar(255) unique not null,
  body text not null,
  type page_component_type not null,
  is_deleted boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz,
  published_on timestamptz
);

create table page_components_to_page_mappings(
  id integer not null generated always as identity primary key,
  page_id integer not null references pages(id),
  page_component_id integer not null references page_components(id),
  created_on timestamptz not null default now(),
  updated_on timestamptz
);