create table sidebar_menus(
  id integer not null generated always as identity primary key,
  version integer not null default 1,
  title varchar(100) not null unique,
  slug varchar(32) not null unique,
  is_enabled boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);

create table sidebar_menu_items(
  id integer not null generated always as identity primary key,
  sidebar_menu_id integer not null references sidebar_menus(id),
  title varchar(30) not null,
  body varchar(30) not null,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);