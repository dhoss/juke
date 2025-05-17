insert into authors(user_name, email) values('Devin', 'devin.austin@gmail.com');

insert into pages(author_id, title, slug, published_on)
values((select id from authors where email = 'devin.austin@gmail.com'), 'Juke CMS', 'base-page', now());

insert into pages(author_id, title, slug, published_on)
values((select id from authors where email = 'devin.austin@gmail.com'), 'Juke CMS - Welcome', 'front-page', now());

insert into page_components(author_id, title, body, slug, type, published_on)
values((select id from authors where email = 'devin.austin@gmail.com'), 'Welcome', 'Welcome to Juke', 'welcome', 'motd', now());

insert into page_components(author_id, title, body, slug, type, published_on)
values(
  (select id from authors where email = 'devin.austin@gmail.com'),
  'About Juke', 'Insert ''about juke'' text here', 'about-juke', 'news', now());

insert into page_components_to_page_mappings(page_id, page_component_id)
values((select id from pages where slug = 'front-page'), (select id from page_components where slug = 'welcome'));

insert into page_components_to_page_mappings(page_id, page_component_id)
values((select id from pages where slug = 'front-page'), (select id from page_components where slug = 'about-juke'));

insert into layouts(slug, is_enabled) values('default', true);

insert into sidebar_menus(title, slug, is_enabled, layout_id)
values("Links", "links", true, (select id from layouts where slug='default'));

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='links'), 'About', '/pages/about');

insert into sidebar_menu_items(sidebar_menus_id, title, body)
values((select id from sidebar_menus where slug='links'), 'Juke Source Code', 'https://github.com/dhoss/juke');

insert into configuration(layout_id) values((select id from layouts where slug='default'));