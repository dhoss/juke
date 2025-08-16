insert into blogs(slug, name, owner, created_on)
values(
  'test-blog',
  'test blog',
  (select id from authors where email = 'devin.austin@gmail.com' limit 1),
  '2025-08-29 15:32:19.380Z'
);

insert into blog_posts(author, blog, title, slug, body, approved, created_on, published_on)
values(
  (select id from authors where email = 'devin.austin@gmail.com' limit 1),
  (select id from blogs where slug = 'test-blog' limit 1),
  'test post',
  'test-post',
  'test post body',
  true,
  '2025-08-29 15:32:19.380Z',
  '2025-08-29 15:32:19.380Z'
);

insert into blog_posts(author, blog, parent, title, slug, body, approved, created_on, published_on)
values(
  (select id from authors where email = 'devin.austin@gmail.com' limit 1),
  (select id from blogs where slug = 'test-blog' limit 1),
  (select id from blog_posts where slug='test-post' limit 1),
  'test post reply',
  'test-post-reply',
  'test post reply body',
  true,
  '2025-08-29 15:32:19.380Z',
  '2025-08-29 15:32:19.380Z'
);

insert into blog_posts(author, blog, parent, title, slug, body, approved, created_on, published_on)
values(
  (select id from authors where email = 'devin.austin@gmail.com' limit 1),
  (select id from blogs where slug = 'test-blog' limit 1),
  (select id from blog_posts where slug='test-post-reply' limit 1),
  'test post reply to reply',
  'test-post-reply-to-reply',
  'test post reply to reply body',
  true,
  '2025-08-29 15:32:19.380Z',
  '2025-08-29 15:32:19.380Z'
);