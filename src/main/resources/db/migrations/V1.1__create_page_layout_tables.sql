-- TODO: layouts and sidebar_menus are almost the exact same, figure out a way to generalize them
create table layouts(
  id integer not null generated always as identity primary key,
  slug varchar(100) not null unique,
  is_enabled boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);

create table sidebar_menus(
  id integer not null generated always as identity primary key,
  layout_id integer not null references layouts(id),
  title varchar(100) not null unique,
  slug varchar(32) not null unique,
  is_enabled boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);

create table sidebar_menu_items(
  id integer not null generated always as identity primary key,
  sidebar_menus_id integer not null references sidebar_menus(id),
  title varchar(30) not null,
  body varchar(30) not null,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);