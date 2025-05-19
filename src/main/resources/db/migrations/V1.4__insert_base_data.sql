-- create initial user
insert into authors(user_name, email) values('Devin', 'devin.austin@gmail.com');

--insert into page_components(author_id, title, body, slug, type, published_on)
--values((select id from authors where email = 'devin.austin@gmail.com'), 'Welcome', 'Welcome to Juke', 'welcome', 'motd', now());
--
--insert into page_components(author_id, title, body, slug, type, published_on)
--values(
--  (select id from authors where email = 'devin.austin@gmail.com'),
--  'About Juke', 'Insert ''about juke'' text here', 'about-juke', 'news', now());
--
--insert into page_components_to_page_mappings(page_id, page_component_id)
--values((select id from pages where slug = 'front-page'), (select id from page_components where slug = 'welcome'));
--
--insert into page_components_to_page_mappings(page_id, page_component_id)
--values((select id from pages where slug = 'front-page'), (select id from page_components where slug = 'about-juke'));

-- create default layout
insert into layouts(slug, is_enabled) values('default', true);

-- insert initial pages
--insert into pages(author_id, title, slug, layout_id, published_on)
--values((select id from authors where email = 'devin.austin@gmail.com'), 'Juke CMS', 'base-page', (select id from layouts where slug = 'default'), now());

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