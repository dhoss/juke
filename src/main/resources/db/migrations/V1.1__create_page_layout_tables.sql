-- TODO: layouts and sidebar_menus are almost the exact same, figure out a way to generalize them
create table layouts(
  id integer not null generated always as identity primary key,
  slug varchar(100) not null unique,
  is_enabled boolean not null default false,
  created_on timestamptz not null default now(),
  updated_on timestamptz
);

-- create default layout
insert into layouts(slug, is_enabled) values('default', true);

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

-- create default menus
insert into sidebar_menus(title, slug, is_enabled, layout_id)
values('Info', 'info', true, (select id from layouts where slug='default'));

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='info'), 'About', '/about.html');

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='info'), 'Juke Source Code', 'https://github.com/dhoss/juke');

insert into sidebar_menus(title, slug, is_enabled, layout_id)
values('Forums', 'forums', true, (select id from layouts where slug='default'));

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='forums'), 'General', '/forums/general');

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='forums'), 'Computers', '/forums/computers');

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='forums'), 'Gaming', '/forums/gaming');
