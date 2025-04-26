insert into authors(user_name, email) values('Devin', 'devin.austin@gmail.com');

insert into pages(author_id, title, slug, published_on)
values((select id from authors where email = 'devin.austin@gmail.com'), 'Juke CMS', 'front-page', now());

insert into page_components(author_id, title, body, type, published_on)
values((select id from authors where email = 'devin.austin@gmail.com'), 'Welcome', 'Welcome to Juke', 'motd', now());

insert into page_components(author_id, title, body, type, published_on)
values(
  (select id from authors where email = 'devin.austin@gmail.com'),
  'About Juke', 'Insert ''about juke'' text here', 'news', now());

insert into page_components_to_page_mappings(page_id, page_component_id)
values((select id from pages where title = 'Juke CMS'), (select id from page_components where title = 'Welcome'));
insert into page_components_to_page_mappings(page_id, page_component_id)
values((select id from pages where title = 'Juke CMS'), (select id from page_components where title = 'About Juke'));