create table page_trees
(
    id           integer      not null generated always as identity primary key,
    author       integer      not null references authors (id),
    title        varchar(200) not null unique,
    slug         varchar(100) not null unique,
    body         varchar      not null,
    parent       integer references page_trees (id),
    approved     boolean default false,
    created_on   timestamptz  not null,
    published_on timestamptz,
    updated_on   timestamptz
);

insert into page_trees(id, author, title, slug, body, parent, approved, created_on, published_on)
    overriding system value
values (1, (select id from users limit 1), 'test root page', 'test-root-page', 'test root page body', null, true,
        '2026-09-06 16:14:20.231 -0600',
        '2026-09-06 16:14:20.231 -0600'),
       (2, (select id from users limit 1), 'test root page first child page', 'test-root-page-first-child-page',
        'test root page first child page body', 1, true,
        '2026-09-06 16:14:20.231 -0600',
        '2026-09-06 16:14:20.231 -0600'),
       (3, (select id from users limit 1), 'test root page first child page first child',
        'test-root-page-first-child-page-first-child',
        'test root page first child page body first child', 2, true,
        '2026-09-06 16:14:20.231 -0600',
        '2026-09-06 16:14:20.231 -0600')
;
