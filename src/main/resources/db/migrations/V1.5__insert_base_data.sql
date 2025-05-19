-- create initial user
insert into authors(user_name, email) values('Devin', 'devin.austin@gmail.com');

-- create default layout
insert into layouts(slug, is_enabled) values('default', true);

insert into pages(author_id, title, slug, layout_id, published_on)
values(
(select id from authors where email = 'devin.austin@gmail.com'),
'Juke CMS - Welcome', 'front-page',
(select id from layouts where slug = 'default'),
now());

insert into pages(author_id, title, slug, layout_id, published_on)
values(
(select id from authors where email = 'devin.austin@gmail.com'),
'Juke CMS - About',
'about',
(select id from layouts where slug = 'default'),
now());

-- create default menus
insert into sidebar_menus(title, slug, is_enabled, layout_id)
values('Info', 'info', true, (select id from layouts where slug='default'));

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='info'), 'About', '/pages/about');

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

-- create configuration
insert into configuration(layout_id) values((select id from layouts where slug='default'));

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
